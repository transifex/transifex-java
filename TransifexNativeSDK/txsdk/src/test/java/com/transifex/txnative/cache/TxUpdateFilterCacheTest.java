package com.transifex.txnative.cache;

import android.os.Build;

import com.transifex.common.LocaleData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

// We need roboelectric to emulate TextUtils
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class TxUpdateFilterCacheTest {

    // The following Translation Maps are based on the table found in TxCacheUpdatePolicy

    private LocaleData.TranslationMap getTranslations1() {
        LocaleData.LocaleStrings strings = new LocaleData.LocaleStrings(10);
        strings.put("a", new LocaleData.StringInfo("a"));
        strings.put("b", new LocaleData.StringInfo("b"));
        strings.put("c", new LocaleData.StringInfo("c"));
        strings.put("d", new LocaleData.StringInfo(""));
        strings.put("e", new LocaleData.StringInfo(""));

        LocaleData.TranslationMap map = new LocaleData.TranslationMap(1);
        map.put("el", strings);

        return map;
    }

    private LocaleData.TranslationMap getTranslations2() {
        LocaleData.LocaleStrings strings = new LocaleData.LocaleStrings(10);
        strings.put("b", new LocaleData.StringInfo("B"));
        strings.put("c", new LocaleData.StringInfo(""));
        strings.put("e", new LocaleData.StringInfo("E"));
        strings.put("f", new LocaleData.StringInfo("F"));
        strings.put("g", new LocaleData.StringInfo(""));

        LocaleData.TranslationMap map = new LocaleData.TranslationMap(1);
        map.put("el", strings);

        return map;
    }

    // This is the expected result of applying getTranslations2() on getTranslations1() using
    // TxUpdateFilterCache.UPDATE_USING_TRANSLATED
    private LocaleData.TranslationMap getTranslationsForUpdateUsingTranslatedGroundTruth() {
        LocaleData.LocaleStrings strings = new LocaleData.LocaleStrings(10);
        strings.put("a", new LocaleData.StringInfo("a"));
        strings.put("b", new LocaleData.StringInfo("B"));
        strings.put("c", new LocaleData.StringInfo("c"));
        strings.put("d", new LocaleData.StringInfo(""));
        strings.put("e", new LocaleData.StringInfo("E"));
        strings.put("f", new LocaleData.StringInfo("F"));

        LocaleData.TranslationMap map = new LocaleData.TranslationMap(1);
        map.put("el", strings);

        return map;
    }

    @Test
    public void testUpdate_replaceAllPolicy_getCorrectMap() {
        int policy = TxUpdateFilterCache.TxCacheUpdatePolicy.REPLACE_ALL;
        TxMemoryCache internalCache = new TxMemoryCache();
        internalCache.update(getTranslations1());
        TxUpdateFilterCache updateFilterCache = new TxUpdateFilterCache(policy, internalCache);
        updateFilterCache.update(getTranslations2());

        assertThat(updateFilterCache.get()).isEqualTo(getTranslations2());
    }

    @Test
    public void testUpdate_updateUsingTranslatedPolicy_getCorrectMap() {
        int policy = TxUpdateFilterCache.TxCacheUpdatePolicy.UPDATE_USING_TRANSLATED;
        TxMemoryCache internalCache = new TxMemoryCache();
        internalCache.update(getTranslations1());
        TxUpdateFilterCache updateFilterCache = new TxUpdateFilterCache(policy, internalCache);
        updateFilterCache.update(getTranslations2());

        assertThat(updateFilterCache.get()).isEqualTo(getTranslationsForUpdateUsingTranslatedGroundTruth());
    }

    @Test
    public void testUpdate_replaceAllAndReadOnlyInternalCachePolicy_keepInternalCacheUnchanged() {
        // This tests makes sure that TxUpdateFilterCache updates the internal cache by calling
        // its update() method and not by accidentally changing the internal cache's map

        int policy = TxUpdateFilterCache.TxCacheUpdatePolicy.REPLACE_ALL;
        TxMemoryCache internalCache = new TxMemoryCache();
        internalCache.update(getTranslations1());
        TxUpdateFilterCache updateFilterCache = new TxUpdateFilterCache(policy, new TxReadonlyCacheDecorator(internalCache));
        updateFilterCache.update(getTranslations2());

        assertThat(updateFilterCache.get()).isEqualTo(getTranslations1());
    }

    @Test
    public void testUpdate_updateUsingTranslatedPolicyAndReadOnlyInternalCache_keepInternalCacheUnchanged() {
        // This tests makes sure that TxUpdateFilterCache updates the internal cache by calling
        // its update() method and not by accidentally changing the internal cache's map

        int policy = TxUpdateFilterCache.TxCacheUpdatePolicy.UPDATE_USING_TRANSLATED;
        TxMemoryCache internalCache = new TxMemoryCache();
        internalCache.update(getTranslations1());
        TxUpdateFilterCache updateFilterCache = new TxUpdateFilterCache(policy, new TxReadonlyCacheDecorator(internalCache));
        updateFilterCache.update(getTranslations2());

        assertThat(updateFilterCache.get()).isEqualTo(getTranslations1());
    }

    @Test
    public void testUpdate_updateUsingTranslatedPolicyAndEmptyInternalCache_addNewLocale() {
        // We make sure that a new locale can be added when UPDATE_USING_TRANSLATED is used.

        int policy = TxUpdateFilterCache.TxCacheUpdatePolicy.UPDATE_USING_TRANSLATED;
        TxMemoryCache internalCache = new TxMemoryCache();
        TxUpdateFilterCache updateFilterCache = new TxUpdateFilterCache(policy, internalCache);
        updateFilterCache.update(getTranslations1());

        assertThat(updateFilterCache.get().getLocales()).containsExactly("el");
        assertThat(updateFilterCache.get("a", "el")).isEqualTo("a");
        assertThat(updateFilterCache.get("d", "el")).isNull();
    }
}