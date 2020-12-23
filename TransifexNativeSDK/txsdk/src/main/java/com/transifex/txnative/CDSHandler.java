package com.transifex.txnative;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

class CDSHandler {

    /**
     * A callback that provides the results of {@link #fetchTranslations(String, FetchTranslationsCallback)}
     * when the operation is complete.
     */
    interface FetchTranslationsCallback {

        /**
         * Called when the operation is complete.
         * <p>
         * If the operation fails, the translationMap will be empty.
         *
         * @param translationMap A {@link com.transifex.txnative.LocaleData.TranslationMap TranslationMap}
         *                       holding the results.
         */
        @WorkerThread
        void onComplete(@NonNull LocaleData.TranslationMap translationMap);
    }

    public static final String CDS_HOST = "https://cds.svc.transifex.net";

    private static final String TAG = CDSHandler.class.getSimpleName();

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
     * Class that contains the result of {@link #fetchLocale(Uri, String)}.
     */
    private static class FetchLocaleResult {
        LocaleData.TxResponseData response;
        HttpURLConnection connection;

        public FetchLocaleResult(LocaleData.TxResponseData response, HttpURLConnection connection) {
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
    public CDSHandler(@NonNull String[] localeCodes,
                      @NonNull String token, @Nullable String secret, @NonNull String csdHost) {
        mLocaleCodes = localeCodes;
        mToken = token;
        mSecret = secret;
        mCdsHost = csdHost;

        mGson = new Gson();
    }

    /**
     *  Fetch translations from CDS.
     *  <p>
     *  The method is asynchronous. The callback is called on a background thread.
     *
     * @param localeCode  An optional locale to fetch translations from; if  set to <code>null</code>,
     *                    it will fetch translations for the locale codes provided in the constructor.
     * @param callback A callback function to call when the operation is complete.
     */
    public void fetchTranslations(@Nullable final String localeCode,
                                  @NonNull final FetchTranslationsCallback callback) {
        try {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    LocaleData.TranslationMap result = fetchTranslationsInternal(localeCode);
                    callback.onComplete(result);
                }
            });
        }
        catch (RejectedExecutionException exception) {
            Log.e(TAG, "Could not execute background task: " + exception);
            callback.onComplete(new LocaleData.TranslationMap(0));
        }
    }

    /**
     * The implementation that {@link #fetchTranslations(String, FetchTranslationsCallback)} calls
     * in a background thread.
     * <p>
     * The method is synchronous and should only run in a background thread.
     *
     * @see #fetchTranslations(String, FetchTranslationsCallback)
     */
    public @NonNull
    LocaleData.TranslationMap fetchTranslationsInternal(@Nullable String localeCode) {
        String[] fetchLocalCodes = (localeCode != null) ? new String[]{localeCode} : mLocaleCodes;
        LocaleData.TranslationMap translations = new LocaleData.TranslationMap(fetchLocalCodes.length);

        Uri cdsHostURI = Uri.parse(mCdsHost).buildUpon().appendEncodedPath("content").build();

        // Check URL
        URL cdsHostURL = null;
        try {
            cdsHostURL = new URL(cdsHostURI.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid CDS host URL: " + cdsHostURI);
        }
        if (cdsHostURL == null) {
            return translations;
        }

        // For each locale
        HttpURLConnection lastConnection = null;
        for (String fetchLocalCode : fetchLocalCodes) {
            FetchLocaleResult results = fetchLocale(cdsHostURI, fetchLocalCode);
            LocaleData.TxResponseData responseData = results.response;
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
    FetchLocaleResult fetchLocale(Uri cdsHostURI, String localeCode) {
        Uri uri = cdsHostURI.buildUpon().appendPath(localeCode).build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid CDS host URL: " + cdsHostURI);
        }
        if (url == null) {
            return new FetchLocaleResult(null, null);
        }

        HttpURLConnection connection = null;

        for (int tries = 0; tries < MAX_RETRIES; tries++) {
            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                Log.e(TAG, "IOException when opening connection for locale " + localeCode + " : " + e);
                return new FetchLocaleResult(null, connection);
            }
            addHeaders(connection, false, null);

            try {
                connection.connect();
                int code = connection.getResponseCode();
                switch (code) {
                    case 200: {
                        String result = Utils.readInputStream(connection.getInputStream());
                        connection.getInputStream().close();

                        LocaleData.TxResponseData responseData = null;
                        try {
                            responseData = mGson.fromJson(result, LocaleData.TxResponseData.class);
                        } catch (JsonSyntaxException e) {
                            Log.e(TAG, "Could not parse JSON response to object for locale " + localeCode);
                        }

                        return new FetchLocaleResult(responseData, connection);
                    }
                    case 202:
                        // try one more time
                        continue;
                    default:
                        Log.e(TAG, "Server responded with code " + code + " for locale " + localeCode);
                        return new FetchLocaleResult(null, connection);
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException for locale " + localeCode + " : " + e);

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

        connection.addRequestProperty("Content-type", "application/json");
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
