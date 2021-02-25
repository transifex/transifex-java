package com.transifex.common;

import org.junit.Test;

import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;

public class LocaleDataTest {

    public static LocaleData.LocaleStrings getElLocaleStrings() {
        HashMap<String, LocaleData.StringInfo> map = new HashMap<>();
        map.put("test_key", new LocaleData.StringInfo("Καλημέρα"));
        map.put("test_key3", new LocaleData.StringInfo(""));
        return new LocaleData.LocaleStrings(map);
    }

    public static LocaleData.LocaleStrings getEsLocaleStrings() {
        HashMap<String, LocaleData.StringInfo> map = new HashMap<>();
        map.put("test_key", new LocaleData.StringInfo("Buenos días"));
        map.put("test_key3", new LocaleData.StringInfo(""));
        return new LocaleData.LocaleStrings(map);
    }

    public static LocaleData.TranslationMap getElEsTranslationMap() {
        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("el", getElLocaleStrings());
        translationMap.put("es", getEsLocaleStrings());
        return translationMap;
    }

    public static LocaleData.TranslationMap getElTranslationMap() {
        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(1);
        translationMap.put("el", getElLocaleStrings());
        return translationMap;
    }

    public static LocaleData.TranslationMap getEsTranslationMap() {
        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(1);
        translationMap.put("es", getElLocaleStrings());
        return translationMap;
    }

    @Test
    public void testStringInfoHash() {
        LocaleData.StringInfo a = new LocaleData.StringInfo("a");
        LocaleData.StringInfo a2 = new LocaleData.StringInfo("a");

        assertThat(a.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    public void testStringInfoEquals() {
        LocaleData.StringInfo a = new LocaleData.StringInfo("a");
        LocaleData.StringInfo a2 = new LocaleData.StringInfo("a");
        LocaleData.StringInfo b = new LocaleData.StringInfo("b");

        assertThat(a).isEqualTo(a2);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    public void testLocaleStringsHash() {
        LocaleData.LocaleStrings a = getElLocaleStrings();
        LocaleData.LocaleStrings a2 = getElLocaleStrings();

        assertThat(a.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    public void testLocaleStringsEquals() {
        LocaleData.LocaleStrings a = getElLocaleStrings();
        LocaleData.LocaleStrings a2 = getElLocaleStrings();
        LocaleData.LocaleStrings b = getEsLocaleStrings();

        assertThat(a).isEqualTo(a2);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    public void testTranslationMapHash() {
        LocaleData.TranslationMap a = getElEsTranslationMap();
        LocaleData.TranslationMap a2 = getElEsTranslationMap();

        assertThat(a.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    public void testTranslationMapEquals() {
        LocaleData.TranslationMap a = getElEsTranslationMap();
        LocaleData.TranslationMap a2 = getElEsTranslationMap();
        LocaleData.TranslationMap b = getElTranslationMap();

        assertThat(a).isEqualTo(a2);
        assertThat(a).isNotEqualTo(b);
    }
}