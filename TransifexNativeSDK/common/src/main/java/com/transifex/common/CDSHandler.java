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
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * Class that contains the result of {@link #getConnectionForLocale(URI, String)}
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
     * The callback to get the results of {@link #fetchTranslations(String, FetchCallback)}
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
     * @param localeCodes An array of locale codes that can be downloaded from CDS. It should not
     *                    include the source language.
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
    ConnectionData getConnectionForLocale(URI cdsContentURI, String localeCode) {
        URL url = null;
        try {
            URI uri  = new URIBuilderTiny(cdsContentURI).appendPaths(localeCode).build();
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Invalid CDS host URL: " + cdsContentURI);
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
                try {
                    Utils.readInputStream(connection.getErrorStream());
                } catch (IOException ignored) {}

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
     *
     * @see FetchCallback
     */
    public void fetchTranslations(@Nullable String localeCode, @NonNull FetchCallback callback) {
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
            ConnectionData connectionData = getConnectionForLocale(cdsContentURI, fetchLocalCode);
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
     *
     * @return A {@link LocaleData.TranslationMap} object that contains the translations for each
     * locale. If an error occurs, some or all locales will be missing from the translation map.
     */
    @NonNull
    public LocaleData.TranslationMap fetchTranslations(@Nullable String localeCode) {
        ParseFetchedTranslationsCallback fetchTranslationsCallback = new ParseFetchedTranslationsCallback();
        fetchTranslations(localeCode, fetchTranslationsCallback);

        return fetchTranslationsCallback.translationMap;
    }

    /**
     * Pushes the provided source strings to CDS.
     * <p>
     * The method is synchronous.
     *
     * @param postData The data containing the source strings and the purge value.
     *
     * @return a {@link LocaleData.TxPostResponseData} object if data were pushed successfully,
     * <code>null</code> otherwise.
     */
    public @Nullable
    LocaleData.TxPostResponseData pushSourceStrings(@NonNull LocaleData.TxPostData postData) {
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
                case 200: {
                    String result = Utils.readInputStream(connection.getInputStream());
                    LocaleData.TxPostResponseData responseData =
                            mGson.fromJson(result, LocaleData.TxPostResponseData.class);

                    return responseData;
                }
                default:
                    LOGGER.log(Level.SEVERE, "Server responded with code " + code);
                    return null;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IOException: " + e);

            // https://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html
            try {
                String errorResponse = Utils.readInputStream(connection.getErrorStream());
                connection.getErrorStream().close();
            } catch (IOException ignored) {}

            return null;
        } catch(JsonSyntaxException e) {
            LOGGER.log(Level.SEVERE, "Error parsing server response: " + e);

            return null;
        } finally {
            connection.disconnect();
        }
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

        if (etag != null) {
            connection.addRequestProperty("If-None-Match", etag);
        }
    }
}
