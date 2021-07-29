package com.transifex.common;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.moznion.uribuildertiny.URIBuilderTiny;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.TimeLimitExceededException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * CDSHandler enables pushing and pulling strings to/from the CDS.
 */
public class CDSHandler {

    public static final String CDS_HOST = "https://cds.svc.transifex.net";

    private static final String TAG = CDSHandler.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(TAG);

    private static final int MAX_RETRIES = 20;

    // A list of locale codes for the configured languages in the application
    private final String[] mLocaleCodes;

    // The API token to use for connecting to the CDS
    private final String mToken;

    // The API secret to use for connecting to the CDS
    private final String mSecret;

    // The host of the Content Delivery Service
    private final String mCdsHost;

    private final Gson mGson;

    /**
     * Class that contains the result of {@link #getConnectionForLocale(URI, String, Set)}
     */
    private static class ConnectionData {
        HttpURLConnection connection;
        InputStream inputStream;
        Exception exception;

        public ConnectionData(HttpURLConnection connection, InputStream inputStream, Exception exception) {
            this.connection = connection;
            this.inputStream = inputStream;
            this.exception = exception;
        }
    }

    /**
     * The callback to get the results of {@link #fetchTranslations(String, Set, FetchCallback)}
     */
    public interface FetchCallback {

        /**
         * Called once before initiating the connection to CDS. You can get the locales that will be
         * fetched.
         */
        void onFetchingTranslations(@NonNull String[] localeCodes);

        /**
         * Called for each locale when a connection with the CDS is established. The implementation
         * should consume the provided input stream.
         * <p>
         * If an error occurs when establishing the connection, the <code>exception</code> will be
         * supplied and the input stream will be <code>null</code>.
         */
        void onTranslationFetched(@Nullable InputStream inputStream, @NonNull String localeCode,
                                  @Nullable Exception exception);

        /**
         * Called once if an exception occurs during setup.
         */
        void onFailure(@NonNull Exception exception);
    }

    /**
     * Creates a CDSHandler instance.
     *
     * @param localeCodes An array of locale codes that can be downloaded from CDS. The source
     *                    locale can also be included.
     * @param token The API token to use for connecting to the CDS.
     * @param secret The API secret to use for connecting to the CDS.
     * @param csdHost The host of the Content Delivery Service.
     */
    public CDSHandler(@Nullable String[] localeCodes,
                      @NonNull String token, @Nullable String secret, @NonNull String csdHost) {
        mLocaleCodes = localeCodes;
        mToken = token;
        mSecret = secret;
        mCdsHost = csdHost;

        mGson = new Gson();
    }

    /**
     * Establishes a connection to CDS for the specified locale and returns a
     * {@link ConnectionData} object containing the connection and its input stream.
     * <p>
     * If the connection is successful, the <code>inputStream</code> of the returned
     * {@link ConnectionData} should be consumed and closed.
     * <p>
     * If the connection fails, the  input stream will be <code>null</code> and the exception will
     * contain the reason. The connection may not be <code>null</code>.
     *
     * @return A {@link ConnectionData} object containing the connection and it's input
     * stream.
     */
    private @NonNull
    ConnectionData getConnectionForLocale(@NonNull URI cdsContentURI, @NonNull String localeCode,
                                          @Nullable Set<String> tags) {
        URL url = null;
        try {
            URIBuilderTiny uriBuilder  = new URIBuilderTiny(cdsContentURI).appendPaths(localeCode);
            if (tags != null && !tags.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String tag : tags) {
                    sb.append(tag).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                uriBuilder.addRawQueryParameter(Utils.urlEncode("filter[tags]"),
                        Utils.urlEncode(sb.toString()));
            }
            URI uri = uriBuilder.build();
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Invalid CDS host URL: " + cdsContentURI);
            return new ConnectionData(null, null, e);
        } catch ( IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Illegal argument exception for URL: " + cdsContentURI
                    + " with locale: " + localeCode + " , tags: " + tags);
            return new ConnectionData(null, null, e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Unsupported encoding exception for URL: " + cdsContentURI
                    + " with locale: " + localeCode + " , tags: " + tags);
            return new ConnectionData(null, null, e);
        }

        HttpURLConnection connection = null;

        for (int tries = 0; tries < MAX_RETRIES; tries++) {
            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException when opening connection for locale " + localeCode + " : " + e);
                return new ConnectionData(null, null, e);
            }
            addHeaders(connection, false, null);

            try {
                connection.connect();
                int code = connection.getResponseCode();
                switch (code) {
                    case 200: {
                        InputStream inputStream = connection.getInputStream();
                        return new ConnectionData(connection, inputStream, null);
                    }
                    case 202:
                        // try one more time
                        continue;
                    default:
                        LOGGER.log(Level.SEVERE, "Server responded with code " + code + " for locale " + localeCode);
                        return new ConnectionData(connection, null, new IOException("Server responded with " + code));
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException for locale " + localeCode + " : " + e);

                // https://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html
                if (connection.getErrorStream() != null) {
                    try {
                        Utils.readInputStream(connection.getErrorStream());
                    } catch (IOException ignored) {}
                }

                return new ConnectionData(connection, null, e);
            }
        }

        // max tries reached
        return new ConnectionData(connection, null, new IOException("Max retries reached"));
    }

    /**
     * Fetches translations from CDS and supplies the raw input stream to the provided
     * {@link FetchCallback}.
     * <p>
     * The method is synchronous and hence the callback is called during the method execution. The
     * method should only run in a background thread.
     *
     * @param localeCode An optional locale to fetch translations from; if  set to <code>null</code>,
     *                   it will fetch translations for the locale codes provided in the constructor.
     * @param tags An optional set of tags. If defined, only strings that have all of the given tags
     *             will be fetched.
     *
     * @see FetchCallback
     */
    public void fetchTranslations(@Nullable String localeCode, @Nullable Set<String> tags,
                                  @NonNull FetchCallback callback) {
        String[] fetchLocalCodes = (localeCode != null) ? new String[]{localeCode} : mLocaleCodes;
        if (fetchLocalCodes == null) {
            return;
        }

        // Check URL
        URI cdsContentURI = null;
        try {
            cdsContentURI = new URI(mCdsHost);
            cdsContentURI = new URIBuilderTiny(cdsContentURI).appendPaths("content").build();
            URL url = new URL(cdsContentURI.toString());
        } catch (URISyntaxException | MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Invalid CDS host URL: " + mCdsHost);
            callback.onFailure(e);
            return;
        }

        callback.onFetchingTranslations(fetchLocalCodes);

        // For each locale
        HttpURLConnection lastConnection = null;
        for (String fetchLocalCode : fetchLocalCodes) {
            ConnectionData connectionData = getConnectionForLocale(cdsContentURI, fetchLocalCode, tags);
            callback.onTranslationFetched(connectionData.inputStream, fetchLocalCode, connectionData.exception);

            if (connectionData.connection != null) {
                lastConnection = connectionData.connection;
            }
        }

        // Closing the last connection so that the the reusable socket is closed
        if (lastConnection != null) {
            lastConnection.disconnect();
        }
    }

    /**
     * An {@link FetchCallback} implementation that parses the provided input streams to
     * {@link LocaleData.TxPostResponseData} objects.
     * <p>
     * Each parsed object populates a {@link LocaleData.TranslationMap}, which will contain the
     * translations for all parsed locales.
     */
    private class ParseFetchedTranslationsCallback implements FetchCallback {

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(0);

        @Override
        public void onFetchingTranslations(@NonNull String[] localeCodes) {
            translationMap = new LocaleData.TranslationMap(localeCodes.length);
        }

        @Override
        public void onTranslationFetched(@Nullable InputStream inputStream, @NonNull String localeCode,
                                         @Nullable Exception exception) {
            if (inputStream == null) {
                return;
            }

            // Parse input stream with Gson
            LocaleData.TxPullResponseData responseData = null;
            Reader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                responseData = mGson.fromJson(reader, LocaleData.TxPullResponseData.class);
            } catch (JsonSyntaxException e) {
                LOGGER.log(Level.SEVERE, "Could not parse JSON response to object for locale " + localeCode);
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.SEVERE, "Server responded with unsupported encoding for locale " + localeCode);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    else {
                        inputStream.close();
                    }
                } catch (IOException ignored) {}
            }

            if (responseData == null) {
                return;
            }

            translationMap.put(localeCode, new LocaleData.LocaleStrings(responseData.data));
        }

        @Override
        public void onFailure(@NonNull Exception exception) {}
    }

    /**
     * Fetches translations from CDS.
     * <p>
     * The method is synchronous and should only run in a background thread.
     *
     * @param localeCode  An optional locale to fetch translations from; if  set to <code>null</code>,
     *                    it will fetch translations for the locale codes provided in the constructor.
     * @param tags An optional set of tags. If defined, only strings that have all of the given tags
     *             will be fetched.
     *
     * @return A {@link LocaleData.TranslationMap} object that contains the translations for each
     * locale. If an error occurs, some or all locales will be missing from the translation map.
     */
    @NonNull
    public LocaleData.TranslationMap fetchTranslations(@Nullable String localeCode, @Nullable Set<String> tags) {
        ParseFetchedTranslationsCallback fetchTranslationsCallback = new ParseFetchedTranslationsCallback();
        fetchTranslations(localeCode, tags, fetchTranslationsCallback);

        return fetchTranslationsCallback.translationMap;
    }

    /**
     * Pushes the provided source strings to CDS and waits until the server completes the processing
     * of the pushed strings.
     * <p>
     * The method is synchronous.
     *
     * @param postData The data structure containing the source strings and the purge value.
     *
     * @return A {@link LocaleData.TxJobStatus} object containing the server's response. The
     * caller should check the job status and act according to the
     * <a href="https://github.com/transifex/transifex-delivery/#job-status">CDS documentation</a>.
     * The job status can be either <code>"completed"</code> or <code>"failed"</code>.
     * If everything fails, <code>null</code> is returned.
     *
     * @throws TimeLimitExceededException When the server takes longer than 20 seconds to complete
     * processing the job.
     */
    public @Nullable
    LocaleData.TxJobStatus pushSourceStrings(@NonNull LocaleData.TxPostData postData) throws TimeLimitExceededException {
        LocaleData.TxPostResponseData response = pushSourceStringsInternal(postData);
        if (response == null) {
            return null;
        }
        return getJobStatus(response);
    }

    /**
     * Pushes the provided source strings to CDS.
     * <p>
     * The method is synchronous.
     *
     * @param postData The data structure containing the source strings and the purge value.
     *
     * @return A {@link LocaleData.TxPostResponseData} object containing the job id that handles
     * the pushed strings. You can query the job status using
     * {@link #getJobStatus(LocaleData.TxPostResponseData)}.
     *
     * @see <a href="https://github.com/transifex/transifex-delivery/#push-content">
     *     https://github.com/transifex/transifex-delivery/#push-content</a>
     */
    private @Nullable LocaleData.TxPostResponseData pushSourceStringsInternal(@NonNull LocaleData.TxPostData postData) {
        // Check URL
        URL url = null;
        try {
            URI cdsContentURI = new URI(mCdsHost);
            cdsContentURI = new URIBuilderTiny(cdsContentURI).appendPaths("content").build();
            url = new URL(cdsContentURI.toString());
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Invalid CDS host URL: " + mCdsHost);
            return null;
        }

        // Post data
        LocaleData.TxPostResponseData responseData = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IOException when opening connection: " + e);
            return null;
        }

        addHeaders(connection, true, null);

        try {
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            mGson.toJson(postData, writer);
            writer.close();

            int code = connection.getResponseCode();

            switch (code) {
                case 202: {
                    String response = Utils.readInputStream(connection.getInputStream());
                    responseData = mGson.fromJson(response, LocaleData.TxPostResponseData.class);
                    if (responseData.data == null || responseData.data.links == null
                            || responseData.data.links.job == null
                            || responseData.data.links.job.isEmpty()) {
                        LOGGER.log(Level.SEVERE, "Invalid server response" );
                        return null;
                    }
                    return responseData;
                }
                case 409: {
                    LOGGER.log(Level.SEVERE, "Server responded with code " + code + "\n"
                            + "Another content upload is already in progress");
                    if (connection.getErrorStream() != null) {
                        Utils.readInputStream(connection.getErrorStream());
                    }
                    return null;
                }
                default:
                    LOGGER.log(Level.SEVERE, "Server responded with code " + code);
                    return null;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IOException: " + e);

            // https://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html
            if (connection.getErrorStream() != null) {
                try {
                    Utils.readInputStream(connection.getErrorStream());
                } catch (IOException ignored) {
                }
            }

            return null;
        } catch(JsonSyntaxException e) {
            LOGGER.log(Level.SEVERE, "Error parsing server response: " + e);

            return null;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Queries the job status after a call to {@link #pushSourceStringsInternal(LocaleData.TxPostData)}
     * and waits until the job is completed.
     * <p>
     * The method is synchronous.
     *
     * @param responseData The server's response after a call to
     * {@link #pushSourceStringsInternal(LocaleData.TxPostData)}.
     *
     * @return The job status object or <code>null</code> if everything failed. The job status can
     * be either <code>"completed"</code> or <code>"failed"</code>.
     *
     * @throws TimeLimitExceededException When the server takes longer than 20 seconds to complete
     * processing the job.
     *
     * @see <a href="https://github.com/transifex/transifex-delivery/#job-status">
     *     https://github.com/transifex/transifex-delivery/#job-status</a>
     */
    private @Nullable LocaleData.TxJobStatus getJobStatus(@NonNull LocaleData.TxPostResponseData responseData) throws TimeLimitExceededException {
        URL url = null;
        try {
            URI cdsContentURI = new URI(mCdsHost);
            cdsContentURI = new URIBuilderTiny(cdsContentURI).appendRawPaths(responseData.data.links.job).build();
            url = new URL(cdsContentURI.toString());
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Invalid CDS host URL: " + mCdsHost);
            return null;
        }

        HttpURLConnection connection = null;

        for (int tries = 0; tries < MAX_RETRIES; tries++) {
            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException when opening connection: " + e);
                return null;
            }

            addHeaders(connection, true, null);

            try {
                connection.connect();
                int code = connection.getResponseCode();
                switch (code) {
                    case 200: {
                        String result = Utils.readInputStream(connection.getInputStream());
                        LocaleData.TxJobStatus jobStatus = mGson.fromJson(result, LocaleData.TxJobStatus.class);
                        if (jobStatus.data.status.equals("pending")
                                || jobStatus.data.status.equals("processing")) {
                            if (tries > 0) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ignore) {}
                            }
                            continue;
                        }

                        return jobStatus;
                    }
                    default:
                        LOGGER.log(Level.SEVERE, "Server responded with code " + code);
                        return null;
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException:" + e);

                // https://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html
                if (connection.getErrorStream() != null) {
                    try {
                        Utils.readInputStream(connection.getErrorStream());
                    } catch (IOException ignored) {}
                }

                return null;
            }
            finally {
                connection.disconnect();
            }
        }

        throw new TimeLimitExceededException("Server is still processing the pushed strings");
    }

    /**
     * Adds headers to the provided HttpURLConnection
     *
     * @param connection the HttpURLConnection to add headers to
     * @param withSecret if true, the Bearer authorization header will also include the secret,
     *                   otherwise it will only use the token
     * @param etag an optional etag to include for optimization
     */
    private void addHeaders(@NonNull HttpURLConnection connection, boolean withSecret, @Nullable String etag) {
        // No need to specify "Accept-Encoding" with "gzip", because HTTPURLConnection will add it
        // automatically and handle it transparently for us.

        connection.addRequestProperty("Content-type", "application/json; charset=utf-8");

        if (withSecret) {
            connection.addRequestProperty("Authorization", "Bearer " + mToken + ":" + mSecret);
        }
        else {
            connection.addRequestProperty("Authorization", "Bearer " + mToken);
        }

        connection.addRequestProperty("x-native-sdk", "mobile/android/" + BuildProperties.getSDKVersion());
        connection.addRequestProperty("Accept-version", "v2");

        if (etag != null) {
            connection.addRequestProperty("If-None-Match", etag);
        }
    }
}
