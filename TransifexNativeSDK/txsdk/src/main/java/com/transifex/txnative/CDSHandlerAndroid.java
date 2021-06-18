package com.transifex.txnative;

import android.util.Log;

import com.transifex.common.CDSHandler;
import com.transifex.common.LocaleData;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

/**
 * A class that extends {@link CDSHandler} by adding a method that can fetch translations
 * asynchronously.
 *
 * @see CDSHandler
 */
public class CDSHandlerAndroid extends CDSHandler {

    private static final String TAG = CDSHandler.class.getSimpleName();

    private final Executor mExecutor;

    /**
     * A callback that provides the results of {@link #fetchTranslationsAsync(String, FetchTranslationsCallback)}
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
     * @param localeCodes An array of locale codes that can be downloaded from CDS. The source
     *                    locale can also be included.
     * @param token       The API token to use for connecting to the CDS.
     * @param secret      The API secret to use for connecting to the CDS.
     * @param csdHost     The host of the Content Delivery Service.
     */
    public CDSHandlerAndroid(@Nullable String[] localeCodes, @NonNull String token, @Nullable String secret, @NonNull String csdHost) {
        super(localeCodes, token, secret, csdHost);
        mExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     *  Fetch translations from CDS.
     *  <p>
     *  The method is asynchronous. The callback is called on a background thread.
     *
     * @param localeCode  An optional locale to fetch translations from; if  set to <code>null</code>,
     *                    it will fetch translations for the locale codes provided in the constructor.
     * @param tags An optional set of tags. If defined, only strings that have all of the given tags
     *             will be fetched.
     * @param callback A callback function to call when the operation is complete.
     */
    public void fetchTranslationsAsync(@Nullable final String localeCode,
                                       @Nullable final Set<String> tags,
                                  @NonNull final FetchTranslationsCallback callback) {
        try {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    LocaleData.TranslationMap result = fetchTranslations(localeCode, tags);
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
