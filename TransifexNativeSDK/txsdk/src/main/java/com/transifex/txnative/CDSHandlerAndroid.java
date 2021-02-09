package com.transifex.txnative;

import android.os.AsyncTask;
import android.util.Log;

import com.transifex.common.CDSHandler;
import com.transifex.common.LocaleData;

import java.util.concurrent.RejectedExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

/**
 * A class that extends {@link CDSHandler} by adding an async method that uses Android's AsyncTask.
 *
 * @see CDSHandler
 */
public class CDSHandlerAndroid extends CDSHandler {

    private static final String TAG = CDSHandler.class.getSimpleName();

    /**
     * A callback that provides the results of {@link #fetchTranslationsAsync(String, FetchTranslationsCallback)} (String, FetchTranslationsCallback)}
     * when the operation is complete.
     */
    interface FetchTranslationsCallback {

        /**
         * Called when the operation is complete.
         * <p>
         * If the operation fails, the translationMap will be empty.
         *
         * @param translationMap A {@link com.transifex.common.LocaleData.TranslationMap TranslationMap}
         *                       holding the results.
         */
        @WorkerThread
        void onComplete(@NonNull LocaleData.TranslationMap translationMap);
    }

    /**
     * Creates a CDSHandler instance.
     *
     * @param localeCodes An array of locale codes that can be downloaded from CDS. It should not
     *                    include the source language.
     * @param token       The API token to use for connecting to the CDS.
     * @param secret      The API secret to use for connecting to the CDS.
     * @param csdHost     The host of the Content Delivery Service.
     */
    public CDSHandlerAndroid(@Nullable String[] localeCodes, @NonNull String token, @Nullable String secret, @NonNull String csdHost) {
        super(localeCodes, token, secret, csdHost);
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
    public void fetchTranslationsAsync(@Nullable final String localeCode,
                                  @NonNull final FetchTranslationsCallback callback) {
        try {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    LocaleData.TranslationMap result = fetchTranslations(localeCode);
                    callback.onComplete(result);
                }
            });
        }
        catch (RejectedExecutionException exception) {
            Log.e(TAG, "Could not execute background task: " + exception);
            callback.onComplete(new LocaleData.TranslationMap(0));
        }
    }
}
