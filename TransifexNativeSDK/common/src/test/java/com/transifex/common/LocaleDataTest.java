package com.transifex.common;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static com.google.common.truth.Truth.assertThat;

public class LocaleDataTest {

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
        LocaleData.LocaleStrings a = StringTestData.getElLocaleStrings();
        LocaleData.LocaleStrings a2 = StringTestData.getElLocaleStrings();

        assertThat(a.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    public void testLocaleStringsEquals() {
        LocaleData.LocaleStrings a = StringTestData.getElLocaleStrings();
        LocaleData.LocaleStrings a2 = StringTestData.getElLocaleStrings();
        LocaleData.LocaleStrings b = StringTestData.getEsLocaleStrings();

        assertThat(a).isEqualTo(a2);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    public void testLocaleStringsGetMap() {
        LocaleData.LocaleStrings a = StringTestData.getElLocaleStrings();

        HashMap<String, LocaleData.StringInfo> map = a.getMap();

        assertThat(a.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(a.get("test_key3")).isEqualTo("");
    }

    @Test
    public void testLocaleStringsGetMap_alterMap() {
        LocaleData.LocaleStrings a = StringTestData.getElLocaleStrings();

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
        LocaleData.LocaleStrings a = StringTestData.getElLocaleStrings();
        LocaleData.LocaleStrings sameAsA = StringTestData.getElLocaleStrings();

        LocaleData.LocaleStrings copyOfA = new LocaleData.LocaleStrings(a);
        a.put("test_key4", new LocaleData.StringInfo("some text"));

        assertThat(copyOfA).isNotEqualTo(a);
        assertThat(copyOfA).isEqualTo(sameAsA);
    }

    @Test
    public void testTranslationMapPutGet() {
        LocaleData.TranslationMap a = new LocaleData.TranslationMap(10);
        a.put("el", StringTestData.getElLocaleStrings());

        assertThat(a.getLocales()).containsExactly("el");
        assertThat(a.get("el")).isEqualTo(StringTestData.getElLocaleStrings());
    }

    @Test
    public void testTranslationMapHash() {
        LocaleData.TranslationMap a = StringTestData.getElEsTranslationMap();
        LocaleData.TranslationMap a2 = StringTestData.getElEsTranslationMap();

        assertThat(a.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    public void testTranslationMapEquals() {
        LocaleData.TranslationMap a = StringTestData.getElEsTranslationMap();
        LocaleData.TranslationMap a2 = StringTestData.getElEsTranslationMap();
        LocaleData.TranslationMap b = StringTestData.getElTranslationMap();

        assertThat(a).isEqualTo(a2);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    public void testTranslationMapCopyConstructor() {
        LocaleData.TranslationMap a = StringTestData.getElEsTranslationMap();
        LocaleData.TranslationMap sameAsA = StringTestData.getElEsTranslationMap();

        LocaleData.TranslationMap copyOfA = new LocaleData.TranslationMap(a);
        a.put("de", StringTestData.getElLocaleStrings());

        assertThat(copyOfA).isNotEqualTo(a);
        assertThat(copyOfA).isEqualTo(sameAsA);
    }

    @Test
    public void testTxJobStatusIsCompleted_completed_returnTrue() {
        LocaleData.TxJobStatus responseData = new LocaleData.TxJobStatus();
        responseData.data = new LocaleData.TxJobStatus.Data();
        responseData.data.status = "completed";

        assertThat(responseData.isCompleted()).isTrue();
    }

    @Test
    public void testTxJobStatusIsCompleted_failed_returnFalse() {
        LocaleData.TxJobStatus jobStatus = new LocaleData.TxJobStatus();
        jobStatus.data = new LocaleData.TxJobStatus.Data();
        jobStatus.data.status = "failed";

        assertThat(jobStatus.isCompleted()).isFalse();
    }

    @Test
    public void testTxJobStatusHasErrors_emptyErrorArray_returnFalse() {
        LocaleData.TxJobStatus jobStatus = new LocaleData.TxJobStatus();
        jobStatus.data = new LocaleData.TxJobStatus.Data();
        jobStatus.data.errors = new LocaleData.TxJobStatus.Data.Error[0];

        assertThat(jobStatus.hasErrors()).isFalse();
    }

    @Test
    public void testTxJobStatusHasErrors_hasError_returnTrue() {
        LocaleData.TxJobStatus jobStatus = new LocaleData.TxJobStatus();
        jobStatus.data = new LocaleData.TxJobStatus.Data();
        jobStatus.data.errors = new LocaleData.TxJobStatus.Data.Error[]{new LocaleData.TxJobStatus.Data.Error()};

        assertThat(jobStatus.hasErrors()).isTrue();
    }

}