package com.transifex.common;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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

        // Meta should not affect the hash code
        LocaleData.StringInfo.Meta meta = new LocaleData.StringInfo.Meta();
        meta.tags = new HashSet<>(Arrays.asList("tag1", "tag2"));
        LocaleData.StringInfo c = new LocaleData.StringInfo("a", meta);

        assertThat(c.hashCode()).isEqualTo(a.hashCode());
    }

    @Test
    public void testStringInfoEquals() {
        LocaleData.StringInfo a = new LocaleData.StringInfo("a");
        LocaleData.StringInfo a2 = new LocaleData.StringInfo("a");
        LocaleData.StringInfo b = new LocaleData.StringInfo("b");

        assertThat(a).isEqualTo(a2);
        assertThat(a).isNotEqualTo(b);

        LocaleData.StringInfo.Meta meta = new LocaleData.StringInfo.Meta();
        meta.tags = new HashSet<>(Arrays.asList("tag1", "tag2"));
        LocaleData.StringInfo c = new LocaleData.StringInfo("a", meta);
        LocaleData.StringInfo c2 = new LocaleData.StringInfo("a", meta);

        assertThat(c).isEqualTo(c2);
        assertThat(c).isNotEqualTo(a);
    }

    @Test
    public void testStringInfoAppendTags() {
        LocaleData.StringInfo a = new LocaleData.StringInfo("a");
        a.appendTags(new HashSet<>(Arrays.asList("tag1", "tag2")));

        assertThat(a.meta).isNotNull();
        assertThat(a.meta.tags).containsExactly("tag1", "tag2");

        // Check that new tags are added and that each tag is listed once
        a.appendTags(new HashSet<>(Arrays.asList("tag2", "tag3")));
        assertThat(a.meta.tags).containsExactly("tag1", "tag2", "tag3");
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
    public void testLocaleStringsGetMap() {
        LocaleData.LocaleStrings a = getElLocaleStrings();

        HashMap<String, LocaleData.StringInfo> map = a.getMap();

        assertThat(a.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(a.get("test_key3")).isEqualTo("");
    }

    @Test
    public void testLocaleStringsGetMap_alterMap() {
        LocaleData.LocaleStrings a = getElLocaleStrings();

        HashMap<String, LocaleData.StringInfo> map = a.getMap();
        map.put("test_key4", new LocaleData.StringInfo("some text"));

        assertThat(a.get("test_key4")).isEqualTo("some text");
    }

    @Test
    public void testLocaleStringsPutGet() {
        LocaleData.LocaleStrings a = new LocaleData.LocaleStrings(10);
        a.put("test_key", new LocaleData.StringInfo("some text"));

        assertThat(a.get("test_key")).isEqualTo("some text");
    }

    @Test
    public void testLocaleStringsCopyConstructor() {
        LocaleData.LocaleStrings a = getElLocaleStrings();
        LocaleData.LocaleStrings sameAsA = getElLocaleStrings();

        LocaleData.LocaleStrings copyOfA = new LocaleData.LocaleStrings(a);
        a.put("test_key4", new LocaleData.StringInfo("some text"));

        assertThat(copyOfA).isNotEqualTo(a);
        assertThat(copyOfA).isEqualTo(sameAsA);
    }

    @Test
    public void testTranslationMapPutGet() {
        LocaleData.TranslationMap a = new LocaleData.TranslationMap(10);
        a.put("el", getElLocaleStrings());

        assertThat(a.getLocales()).containsExactly("el");
        assertThat(a.get("el")).isEqualTo(getElLocaleStrings());
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

    @Test
    public void testTranslationMapCopyConstructor() {
        LocaleData.TranslationMap a = getElEsTranslationMap();
        LocaleData.TranslationMap sameAsA = getElEsTranslationMap();

        LocaleData.TranslationMap copyOfA = new LocaleData.TranslationMap(a);
        a.put("de", getElLocaleStrings());

        assertThat(copyOfA).isNotEqualTo(a);
        assertThat(copyOfA).isEqualTo(sameAsA);
    }

    @Test
    public void testTxPostResponseDataIsSuccessful_noErrors_successful() {
        LocaleData.TxPostResponseData responseData = new LocaleData.TxPostResponseData();

        assertThat(responseData.isSuccessful()).isTrue();
    }

    @Test
    public void testTxPostResponseDataIsSuccessful_emptyErrorArray_successful() {
        LocaleData.TxPostResponseData responseData = new LocaleData.TxPostResponseData();
        responseData.errors = new LocaleData.TxPostResponseData.Error[0];

        assertThat(responseData.isSuccessful()).isTrue();
    }

    @Test
    public void testTxPostResponseDataIsSuccessful_errors_notSuccessful() {
        LocaleData.TxPostResponseData responseData = new LocaleData.TxPostResponseData();
        responseData.errors = new LocaleData.TxPostResponseData.Error[]{new LocaleData.TxPostResponseData.Error()};

        assertThat(responseData.isSuccessful()).isFalse();
    }

}