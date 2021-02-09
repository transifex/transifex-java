package com.transifex.common;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.moznion.uribuildertiny.URIBuilderTiny;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
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
     * Class that contains the result of {@link #fetchLocale(URI, String)}.
     */
    private static class FetchLocaleResult {
        LocaleData.TxPullResponseData response;
        HttpURLConnection connection;

        public FetchLocaleResult(LocaleData.TxPullResponseData response, HttpURLConnection connection) {
            this.response = response;
            this.connection = connection;
        }
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
     * Fetch translations from CDS.
     * <p>
     * The method is synchronous and should only run in a background thread.
     *
     * @param localeCode  An optional locale to fetch translations from; if  set to <code>null</code>,
     *                    it will fetch translations for the locale codes provided in the constructor.
     */
    @NonNull
    public LocaleData.TranslationMap fetchTranslations(@Nullable String localeCode) {
        String[] fetchLocalCodes = (localeCode != null) ? new String[]{localeCode} : mLocaleCodes;
        if (fetchLocalCodes == null) {
            return new LocaleData.TranslationMap(0);
        }
        LocaleData.TranslationMap translations = new LocaleData.TranslationMap(fetchLocalCodes.length);

        // Check URL
        URI cdsContentURI = null;
        try {
            cdsContentURI = new URI(mCdsHost);
            cdsContentURI = new URIBuilderTiny(cdsContentURI).appendPaths("content").build();
            URL url = new URL(cdsContentURI.toString());
        } catch (URISyntaxException | MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Invalid CDS host URL: " + mCdsHost);
            return translations;
        }

        // For each locale
        HttpURLConnection lastConnection = null;
        for (String fetchLocalCode : fetchLocalCodes) {
            FetchLocaleResult results = fetchLocale(cdsContentURI, fetchLocalCode);
            LocaleData.TxPullResponseData responseData = results.response;
            if (responseData!= null) {
                translations.put(fetchLocalCode, new LocaleData.LocaleStrings(responseData.data));
            }
            if (results.connection != null) {
                lastConnection = results.connection;
            }
        }

        // Closing one connection, will close the reusable socket
        if (lastConnection != null) {
            lastConnection.disconnect();
        }

        return translations;
    }

    /**
     * Fetches the translation data for the specified locale.
     * <p>
     * If the call fails, the <code>data</code> field of the returned
     * {@link FetchLocaleResult} will be <code>"null"</code>.
     *
     * @return A {@link FetchLocaleResult} object containing data.
     */
    private @NonNull
    FetchLocaleResult fetchLocale(URI cdsContentURI, String localeCode) {

        URL url = null;
        try {
            URI uri  = new URIBuilderTiny(cdsContentURI).appendPaths(localeCode).build();
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Invalid CDS host URL: " + cdsContentURI);
        }
        if (url == null) {
            return new FetchLocaleResult(null, null);
        }

        HttpURLConnection connection = null;

        for (int tries = 0; tries < MAX_RETRIES; tries++) {
            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException when opening connection for locale " + localeCode + " : " + e);
                return new FetchLocaleResult(null, connection);
            }
            addHeaders(connection, false, null);

            try {
                connection.connect();
                int code = connection.getResponseCode();
                switch (code) {
                    case 200: {
                        LocaleData.TxPullResponseData responseData = null;
                        Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                        try {
                            responseData = mGson.fromJson(reader, LocaleData.TxPullResponseData.class);
                        } catch (JsonSyntaxException e) {
                            LOGGER.log(Level.SEVERE, "Could not parse JSON response to object for locale " + localeCode);
                        }
                        finally {
                            connection.getInputStream().close();
                        }

                        return new FetchLocaleResult(responseData, connection);
                    }
                    case 202:
                        // try one more time
                        continue;
                    default:
                        LOGGER.log(Level.SEVERE, "Server responded with code " + code + " for locale " + localeCode);
                        return new FetchLocaleResult(null, connection);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException for locale " + localeCode + " : " + e);

                // https://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html
                try {
                    Utils.readInputStream(connection.getErrorStream());
                    connection.getErrorStream().close();
                } catch (IOException ignored) {}

                return new FetchLocaleResult(null, connection);
            }
        }

        // max tries reached
        return new FetchLocaleResult(null, connection);
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
                    connection.getInputStream().close();
                    LocaleData.TxPostResponseData responseData = mGson.fromJson(result, LocaleData.TxPostResponseData.class);

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
            } catch (IOException ignored) {
            }

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
