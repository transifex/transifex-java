package com.transifex.txnative.cache;

import android.content.Context;

import com.transifex.common.LocaleData;
import com.transifex.common.TranslationMapStorage;
import com.transifex.common.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;

import androidx.test.espresso.core.internal.deps.guava.util.concurrent.MoreExecutors;
import androidx.test.platform.app.InstrumentationRegistry;

import static com.google.common.truth.Truth.assertThat;

public class TxStandardCacheTest {

    // The tests rely on the following directory:
    //
    // androidTest/assets/txnative

    Context appContext = null;
    File txNativeCacheDir;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        File cacheDir = appContext.getCacheDir();
        txNativeCacheDir= new File(cacheDir.getPath() +File.separator + TranslationMapStorage.DEFAULT_TRANSLATIONS_DIR_NAME);
        if (txNativeCacheDir.exists()) {
            Utils.deleteDirectory(txNativeCacheDir);
        }
    }

    @After
    public void Teardown() {
        if (txNativeCacheDir.exists()) {
            boolean deleted = Utils.deleteDirectory(txNativeCacheDir);
            if (!deleted) {
                System.out.println("Could not delete tmp dir after test. Next test may fail.");
            }
        }
    }

    // This map is identical to the serialized version stored at: androidTest/assets/txnative
    // and accessed at runtime in the app's Assets folder
    protected static LocaleData.TranslationMap getAssetsEquivalentTranslationMap() {
        LocaleData.TranslationMap map = new LocaleData.TranslationMap(2);

        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(4);
        elStrings.put("test_key", new LocaleData.StringInfo("Καλημέρα"));
        elStrings.put("another_key", new LocaleData.StringInfo("Καλό απόγευμα"));
        elStrings.put("key3", new LocaleData.StringInfo(""));
        map.put("el", elStrings);

        LocaleData.LocaleStrings esStrings = new LocaleData.LocaleStrings(4);
        esStrings.put("test_key", new LocaleData.StringInfo("Buenos días"));
        esStrings.put("another_key", new LocaleData.StringInfo("Buenas tardes"));
        esStrings.put("key3", new LocaleData.StringInfo(""));
        map.put("es", esStrings);

        return map;
    }

    // This map represents translation that could be coming from an updated source such as CDS.
    // It actually contains one locale less than the original.
    private static LocaleData.TranslationMap getUpdatedTranslationMap() {
        LocaleData.TranslationMap map = new LocaleData.TranslationMap(2);

        LocaleData.LocaleStrings esStrings = new LocaleData.LocaleStrings(4);
        esStrings.put("test_key", new LocaleData.StringInfo("Buenos días 2"));
        esStrings.put("another_key", new LocaleData.StringInfo("Buenas tardes"));
        esStrings.put("key3", new LocaleData.StringInfo("Buenas noches"));
        map.put("es", esStrings);

        return map;
    }

    // This is the expected result of applying getUpdatedTranslationMap() on getAssetsEquivalentTranslationMap() using
    // TxUpdateFilterCache.UPDATE_USING_TRANSLATED
    private LocaleData.TranslationMap getTranslationsForUpdateUsingTranslatedGroundTruth() {
        LocaleData.TranslationMap map = new LocaleData.TranslationMap(2);

        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(4);
        elStrings.put("test_key", new LocaleData.StringInfo("Καλημέρα"));
        elStrings.put("another_key", new LocaleData.StringInfo("Καλό απόγευμα"));
        map.put("el", elStrings);

        LocaleData.LocaleStrings esStrings = new LocaleData.LocaleStrings(4);
        esStrings.put("test_key", new LocaleData.StringInfo("Buenos días 2"));
        esStrings.put("another_key", new LocaleData.StringInfo("Buenas tardes"));
        esStrings.put("key3", new LocaleData.StringInfo("Buenas noches"));
        map.put("es", esStrings);

        return map;
    }

    // Make internal executor run in same thread using reflection. If the TxStandardCache
    // implementation changes, we have to update the reflection.
    private boolean changeToSameThreadExecutor(TxCache cache) {
        boolean reflectionSuccess = false;
        try {
            Field executorField = cache.getClass().getDeclaredField("mExecutor");
            executorField.setAccessible(true);
            executorField.set(cache, MoreExecutors.directExecutor());
            reflectionSuccess = true;
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }

        return reflectionSuccess;
    }

    @Test
    // Make sure that we read the translations bundled in the app's Assets correctly
    public void testGetCache_translationsInAssets() {
        TxCache cache = TxStandardCache.getCache(appContext, null, null);
        assertThat(cache.get()).isEqualTo(getAssetsEquivalentTranslationMap());
    }

    @Test
    // Make sure that an update does not change the returned translations and that the updated
    // translations are saved on the app's cache directory
    public void testUpdate_translationsInAssets_returnSameMapAndSaveUpdatedMapOnDisk() {
        TxCache cache = TxStandardCache.getCache(appContext, null, null);
        assertThat(changeToSameThreadExecutor(cache)).isTrue();

        cache.update(getUpdatedTranslationMap());

        // Returned map should remain the same as the one originally loaded from assets
        assertThat(cache.get()).isEqualTo(getAssetsEquivalentTranslationMap());

        // Make sure the updated translations map is written in the expected cache directory
        File cachedTranslationsDir = new File(appContext.getCacheDir() + File.separator + TranslationMapStorage.DEFAULT_TRANSLATIONS_DIR_NAME);
        TranslationMapStorage storage = new TranslationMapStorage(TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
        assertThat(storage.fromDisk(cachedTranslationsDir)).isEqualTo(getUpdatedTranslationMap());
    }

    @Test
    // Make sure that a new cache instance sees the translations in the app's cache directory saved
    // from an update to a previous cache instance
    public void testUpdateAndNewCacheGet_translationsInAssets_returnUpdatedMapFromDisk() {
        // Make a cache that saves the updated translations on disk
        TxCache cache = TxStandardCache.getCache(appContext, null, null);
        assertThat(changeToSameThreadExecutor(cache)).isTrue();

        cache.update(getUpdatedTranslationMap());

        // Make another cache after having the translations saved on disk by the previous cache
        TxCache secondRunCache = TxStandardCache.getCache(appContext, null, null);
        assertThat(changeToSameThreadExecutor(secondRunCache)).isTrue();

        assertThat(secondRunCache.get()).isEqualTo(getUpdatedTranslationMap());
    }

    @Test
    // Make sure that a new cache instance sees the translations in the app's cache directory saved
    // from an update to a previous cache instance
    public void testUpdateAndNewCacheGet_translationsInAssetsAndUpdateUsingTranslatedPolicy_returnUpdatedMapFromDisk() {
        // Make a cache that saves the updated translations on disk. The update polocy used here,
        // doesn't matter
        TxCache cache = TxStandardCache.getCache(appContext, null, null);
        assertThat(changeToSameThreadExecutor(cache)).isTrue();

        cache.update(getUpdatedTranslationMap());

        // Make another cache after having the translations saved on disk by the previous cache
        TxCache secondRunCache = TxStandardCache.getCache(appContext, TxUpdateFilterCache.UPDATE_USING_TRANSLATED, null);
        assertThat(changeToSameThreadExecutor(secondRunCache)).isTrue();

        assertThat(secondRunCache.get()).isEqualTo(getTranslationsForUpdateUsingTranslatedGroundTruth());
    }
}