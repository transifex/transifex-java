package com.transifex.txnative;

import android.content.res.AssetManager;

import com.transifex.common.LocaleData;
import com.transifex.common.TranslationMapStorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A class that extends {@link TranslationMapStorage} so that translations can be read from the
 * application's Assets folder.
 * <p>
 * Translations can be bundled in your application's Assets folder, using the Transifex command-line
 * tool.
 *
 * @see TranslationMapStorage
 */
public class TranslationMapStorageAndroid extends TranslationMapStorage {

    public static final String TAG = TranslationMapStorageAndroid.class.getSimpleName();

    private final AssetFileProvider assetFileProvider;

    /**
     * Creates a new instance.
     *
     * @param manager An instance of Android's {@link AssetManager};
     * @param filename The name of a locale's translation file.
     *
     * @see TranslationMapStorage
     */
    public TranslationMapStorageAndroid(@NonNull AssetManager manager, @NonNull String filename) {
        super(filename);
        assetFileProvider = new AssetFileProvider(manager);
    }

    /**
     * Loads a {@link LocaleData.TranslationMap} from an application's Assets folder under the
     * provided path.
     *
     * @param srcDirectoryPath The path to the directory containing translations in the expected
     *                         format.
     *
     * @return The translation map or <code>null</code> if the directory isn't found or it's empty.
     * If some locales fail to load, they won't be added in the returned map.
     */
    public @Nullable LocaleData.TranslationMap fromAssetsDirectory(@NonNull String srcDirectoryPath) {
        return fromDisk(assetFileProvider, assetFileProvider.getFile(srcDirectoryPath));
    }

    //region AssetFile

    /**
     * An implementation that uses Android's AssetManager.
     */
    private static class AssetFile implements AbstractFile {

        private final AssetManager manager;
        private final String pathname;

        public AssetFile(@NonNull AssetManager manager, @NonNull String pathname) {
            this.manager = manager;
            // Remove trailing slash
            if (pathname.endsWith("/")) {
                pathname = pathname.substring(0, pathname.length() - 1);
            }
            this.pathname = pathname;
        }

        @Nullable
        @Override
        public String[] list() {
            try {
                // If the dir does not exist, it returns an empty list. The java.io.File#list()
                // returns null.
                return manager.list(pathname);
            } catch (IOException ignored) {}

            return null;
        }

        @NonNull
        @Override
        public InputStream open() throws IOException {
            return manager.open(pathname);
        }

        @NonNull
        @Override
        public String getPath() {
            return pathname;
        }

        @NonNull
        @Override
        public String getAbsolutePath() {
            // There is no absolute path
            return pathname;
        }

        @Override
        public boolean isDirectory() {
            // There is no way to check if it's a directory
            return true;
        }
    }

    /**
     * A provider that returns an {@link AssetFile}.
     */
    private static class AssetFileProvider implements AbstractFileProvider {

        private final AssetManager manager;

        public AssetFileProvider(@NonNull AssetManager manager) {
            this.manager = manager;
        }

        @NonNull
        @Override
        public AssetFile getFile(@NonNull String pathname) {
            return new AssetFile(manager, pathname);
        }
    }

    //endregion
}
