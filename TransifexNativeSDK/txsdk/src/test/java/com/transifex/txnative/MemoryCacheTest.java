package com.transifex.txnative;

import org.junit.Test;

import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;

public class MemoryCacheTest {

    private static LocaleData.TranslationMap getDummyTranslationMap() {
        HashMap<String, LocaleData.StringInfo> dic1 = new HashMap<>();
        dic1.put("key1", new LocaleData.StringInfo("val1"));
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(dic1);

        HashMap<String, LocaleData.StringInfo> dic2 = new HashMap<>();
        dic2.put("key1", new LocaleData.StringInfo("val1 es"));
        LocaleData.LocaleStrings esStrings = new LocaleData.LocaleStrings(dic2);

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("el", elStrings);
        translationMap.put("es", esStrings);

        return translationMap;
    }

    private static LocaleData.TranslationMap getDummyTranslationMap2() {
        HashMap<String, LocaleData.StringInfo> dic1 = new HashMap<>();
        dic1.put("key1", new LocaleData.StringInfo("val1 de"));
        LocaleData.LocaleStrings deStrings = new LocaleData.LocaleStrings(dic1);

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("de", deStrings);

        return translationMap;
    }

    @Test
    public void testSupportedLocales_emptyCache() {
        MemoryCache cache = new MemoryCache();

        assertThat(cache.getSupportedLocales()).isEmpty();
    }

    @Test
    public void testSupportedLocales() {
        MemoryCache cache = new MemoryCache();
        cache.update(getDummyTranslationMap());

        assertThat(cache.getSupportedLocales()).containsExactly("el", "es");
    }

    @Test
    public void testGet_emptyCache() {
        MemoryCache cache = new MemoryCache();

        assertThat(cache.get("key1", "el")).isNull();
    }

    @Test
    public void testGet_localeNotSupported() {
        MemoryCache cache = new MemoryCache();
        cache.update(getDummyTranslationMap());

        assertThat(cache.get("key1", "de")).isNull();
    }

    @Test
    public void testGet_nullLocale() {
        MemoryCache cache = new MemoryCache();
        cache.update(getDummyTranslationMap());

        assertThat(cache.get("key1", null)).isNull();
    }

    @Test
    public void testGet() {
        MemoryCache cache = new MemoryCache();
        cache.update(getDummyTranslationMap());

        assertThat(cache.get("key1", "el")).isEqualTo("val1");
    }

    @Test
    public void testGet_updateCalledMultipleTimes() {
        MemoryCache cache = new MemoryCache();
        cache.update(getDummyTranslationMap());

        cache.update(getDummyTranslationMap2());

        assertThat(cache.get("key1", "el")).isNull();
    }

}