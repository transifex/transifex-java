package com.transifex.txnative.cache;

import android.util.Log;

import com.transifex.common.LocaleData;
import com.transifex.common.TranslationMapStorage;
import com.transifex.common.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Decorator class responsible for storing any updates of the translations to a directory specified
 * in the constructor.
 * <p>
 * Storing the translations happens asynchronously on a background thread after
 * {@link #update(LocaleData.TranslationMap)} is called.
 */
public class TxFileOutputCacheDecorator extends TxDecoratorCache {

    public static final String TAG = TxFileOutputCacheDecorator.class.getSimpleName();

    private final File mDstDirectory;
    private final Executor mExecutor;

    /**
     * Creates a new instance with a specific directory for storing the translations to the disk
     * and an internal cache.
     *
     * @param dstDirectory The destination directory to write the translations to when the
     * {@link #update(LocaleData.TranslationMap)} is called.
     * @param internalCache The internal cache.
     */
    public TxFileOutputCacheDecorator(@NonNull File dstDirectory, @NonNull TxCache internalCache) {
        this(null, dstDirectory, internalCache);
    }


    /**
     * Creates a new instance with a specific directory for storing the translations to the disk
     * and an internal cache.
     *
     * @param executor The executor that will run the IO operations; if <code>null</code> is
     *                 provided, {@link Executors#newSingleThreadExecutor()} is used.
     * @param dstDirectory  The destination directory to write the translations to when the
     *                      {@link #update(LocaleData.TranslationMap)} is called.
     * @param internalCache The internal cache.
     */
    protected TxFileOutputCacheDecorator(@Nullable Executor executor, @NonNull File dstDirectory,
                                         @NonNull TxCache internalCache) {
        super(internalCache);
        mExecutor = (executor != null) ? executor : Executors.newSingleThreadExecutor();
        mDstDirectory = dstDirectory;
    }

    /**
     * Updates the cache with the provided translations and writes them to the specified directory
     * after clearing its content, if any.
     * <p>
     * For the serialization and writing of the translations on disk, {@link TranslationMapStorage}
     * is used internally. Each translation file uses "txstrings.json" as filename. Unlike
     * TranslationMapStorage, pre-existing translations are not kept.
     */
    @Override
    public void update(@NonNull final LocaleData.TranslationMap translationMap) {
        super.update(translationMap);

        try {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // Delete existing translation files
                    if (mDstDirectory.isDirectory()) {
                        Utils.deleteDirectoryContents(mDstDirectory);
                    }
                    // Write translation files
                    TranslationMapStorage storage = new TranslationMapStorage(TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
                    HashMap<String, File> files = storage.toDisk(translationMap, mDstDirectory);
                }
            });
        }
        catch (RejectedExecutionException exception) {
            Log.e(TAG, "Could not store updated translations: " + exception);
        }
    }
}
