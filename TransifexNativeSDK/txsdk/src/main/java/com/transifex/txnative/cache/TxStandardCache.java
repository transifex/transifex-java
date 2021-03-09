package com.transifex.txnative.cache;

import android.content.Context;

import com.transifex.common.TranslationMapStorage;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The standard cache configuration that the TxNative SDK is initialized with, if no other cache is
 * provided.
 * <p>
 * TxStandardCache is backed by a {@link TxMemoryCache} which is initialized with existing translation
 * files from the app's Assets folder and the app's cache directory in this specific order. When
 * the cache is updated (when new translations become available after calling
 * {@link com.transifex.txnative.TxNative#fetchTranslations(String)}), TxStandardCache stores the new
 * translations in the app's cache directory. The in-memory translations are not affected by the
 * update though. A new instance of TxStandardCache has to be created (after an app restart by default)
 * to read the new translations saved in the app's cache directory
 */
public class TxStandardCache {

    /**
     * Creates a cache with the configuration explained in {@link TxStandardCache}.
     *
     * @param context The app's context.
     * @param updatePolicy The update policy to be used when initializing the internal memory
     *                     cache with the stored contents from disk. If set to <code>null</code>,
     *                     {@link TxUpdateFilterCache#REPLACE_ALL} is used.
     * @param cachedTranslationsDirectory The directory where the cache will store new translations
     *                                    when available and read translations from when initialized.
     *                                    If set to <code>null</code> it uses a "txnative" folder in
     *                                    the app's internal cache directory.
     *
     * @return A TxCache instance.
     */
    public static TxCache getCache(@NonNull Context context,
                                   @Nullable @TxUpdateFilterCache.TxCacheUpdatePolicy Integer updatePolicy,
                                   @Nullable File cachedTranslationsDirectory) {

        if (updatePolicy == null) {
            updatePolicy = TxUpdateFilterCache.REPLACE_ALL;
        }
        if (cachedTranslationsDirectory == null) {
            cachedTranslationsDirectory = new File(context.getCacheDir() + File.separator
                    + TranslationMapStorage.DEFAULT_TRANSLATIONS_DIR_NAME);
        }

        TxTranslationsProvider[] providers = new TxDiskTranslationsProvider[] {
                new TxDiskTranslationsProvider(
                        context.getAssets(),
                        TranslationMapStorage.DEFAULT_TRANSLATIONS_DIR_NAME),
                new TxDiskTranslationsProvider(cachedTranslationsDirectory)
        };

        return new TxFileOutputCacheDecorator(
                cachedTranslationsDirectory,
                new TxReadonlyCacheDecorator(
                        new TxProviderBasedCache(
                                providers,
                                new TxUpdateFilterCache(
                                        updatePolicy,
                                        new TxMemoryCache()
                                )
                        )
                )
        );
    }

}
