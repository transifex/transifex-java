package com.transifex.txnative.cache;

import android.content.res.AssetManager;
import android.util.Log;

import com.transifex.common.LocaleData;
import com.transifex.common.TranslationMapStorage;
import com.transifex.txnative.TranslationMapStorageAndroid;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Translations provider that loads translations from disk or the application's raw asset files
 * depending on the constructor used.
 * <p>
 * The directory should contain the translations in the format detailed in
 * {@link TranslationMapStorage}.
 * <p>
 * If an error occurs during initialization, {@link #getTranslations()} will return
 * <code>null</code>.
 */
public class TxDiskTranslationsProvider implements TxTranslationsProvider {

    public static final String TAG = TxDiskTranslationsProvider.class.getSimpleName();

    private final LocaleData.TranslationMap mTranslations;

    /**
     * Initializes the provider with a file directory containing translations and loads them
     * synchronously.
     *
     * @param srcDirectory The directory containing translations in the expected format.
     */
    public TxDiskTranslationsProvider(@NonNull File srcDirectory) {
        // Make a check to avoid TranslationMapStorage complaining about directory not existing.
        if (!srcDirectory.isDirectory()) {
            Log.d(TAG, "Translations directory does not exist yet: " + srcDirectory.getPath());
            mTranslations = null;
            return;
        }

        TranslationMapStorage storage = new TranslationMapStorage(TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
        mTranslations = storage.fromDisk(srcDirectory);
    }

    /**
     * Initializes the provider with a directory under the application's raw asset files and loads
     * the translations synchronously.
     *
     * @param manager An asset manager instance.
     * @param srcDirectoryPath The path to the directory containing translations in the expected
     *                         format.
     */
    public TxDiskTranslationsProvider(@NonNull AssetManager manager, @NonNull String srcDirectoryPath) {
        // Make a check and print a debug log
        boolean dirContainsTranslations = false;
        try {
            String[] files = manager.list(srcDirectoryPath);
            dirContainsTranslations = files.length != 0;
        } catch (IOException ignored) {
        }
        if (!dirContainsTranslations) {
            Log.d(TAG, "No translations exist in the Assets folder: " + srcDirectoryPath);
            mTranslations = null;
            return;
        }

        TranslationMapStorageAndroid storage = new TranslationMapStorageAndroid(manager,
                TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
        mTranslations = storage.fromAssetsDirectory(srcDirectoryPath);
    }

    @Override
    @Nullable
    public LocaleData.TranslationMap getTranslations() {
        return mTranslations;
    }
}
