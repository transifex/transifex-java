package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import org.junit.Test;

import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;

public class TxMemoryCacheTest {

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
    public void testGet_emptyCache_returnNullString() {
        TxMemoryCache cache = new TxMemoryCache();

        assertThat(cache.get("key1", "el")).isNull();
    }

    @Test
    public void testGet_localeNotSupported_returnNullString() {
        TxMemoryCache cache = new TxMemoryCache();
        cache.update(getDummyTranslationMap());

        assertThat(cache.get("key1", "de")).isNull();
    }

    @Test
    public void testGet_normal_returnString() {
        TxMemoryCache cache = new TxMemoryCache();
        cache.update(getDummyTranslationMap());

        assertThat(cache.get("key1", "el")).isEqualTo("val1");
    }

    @Test
    public void testGet_updateCalledMultipleTimes_returnStringFromLatestUpdate() {
        TxMemoryCache cache = new TxMemoryCache();
        cache.update(getDummyTranslationMap());

        cache.update(getDummyTranslationMap2());

        assertThat(cache.get("key1", "el")).isNull();
        assertThat(cache.get("key1", "de")).isEqualTo("val1 de");
    }

    @Test
    public void testGetAll_normal() {
        TxMemoryCache cache = new TxMemoryCache();
        cache.update(getDummyTranslationMap());

        assertThat(cache.get()).isEqualTo(getDummyTranslationMap());
    }

    @Test
    public void testGetAll_emptyCache_returnEmptyMap() {
        TxMemoryCache cache = new TxMemoryCache();

        assertThat(cache.get()).isNotNull();
        assertThat(cache.get().getLocales()).isEmpty();
    }
}