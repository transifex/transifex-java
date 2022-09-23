package com.transifex.common;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.naming.TimeLimitExceededException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;

public class CDSHandlerTest {

    private CDSMockHelper cdsMock = null;

    @Before
    public void setUp() {
        cdsMock = new CDSMockHelper();
        cdsMock.setUpServer();
    }

    @After
    public void Teardown() {
        if (cdsMock != null) {
            cdsMock.teardownServer();
        }
    }

    private static LocaleData.TxPostData getPostData() {
        LinkedHashMap<String, LocaleData.StringInfo> data = new LinkedHashMap<>();
        data.put("key1", new LocaleData.StringInfo("This is a source string."));
        data.put("key2", new LocaleData.StringInfo("Using special \ncharacters εδώ."));

        LocaleData.TxPostData.Meta meta = new LocaleData.TxPostData.Meta();
        meta.purge = true;

        return new LocaleData.TxPostData(data, meta);
    }

    private static class DummyFetchCallback implements CDSHandler.FetchCallback {

        boolean onFetchingTranslationsCalled;
        boolean onTranslationFetchedCalled;
        boolean onFailureCalled;

        @Override
        public void onFetchingTranslations(@NonNull String[] localeCodes) {
            onFetchingTranslationsCalled = true;
        }

        @Override
        public void onTranslationFetched(@Nullable InputStream inputStream, @NonNull String localeCode, @Nullable Exception exception) {
            onTranslationFetchedCalled = true;
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            onFailureCalled = true;
        }
    }

    @Test
    public void testFetchTranslationsCallback_badURL() {
        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, "invalidHostURL");

        DummyFetchCallback callback = new DummyFetchCallback();

        cdsHandler.fetchTranslations(null, null, callback);

        assertThat(callback.onFetchingTranslationsCalled).isFalse();
        assertThat(callback.onFetchingTranslationsCalled).isFalse();
        assertThat(callback.onFailureCalled).isTrue();
    }

    @Test
    public void testFetchTranslationsCallback_normalResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());

        DummyFetchCallback callback = new DummyFetchCallback() {

            @Override
            public void onFetchingTranslations(@NonNull String[] localeCodes) {
                super.onFetchingTranslations(localeCodes);
                assertThat(localeCodes).asList().containsExactly("el", "es");
            }

            @Override
            public void onTranslationFetched(@Nullable InputStream inputStream, @NonNull String localeCode, @Nullable Exception exception) {
                super.onTranslationFetched(inputStream, localeCode, exception);

                assertThat(localeCode).isAnyOf("el", "es");

                String stringResponse = null;
                try {
                    stringResponse = Utils.readInputStream(inputStream);
                } catch (IOException ignored) {}
                if (localeCode.equals("el")) {
                    assertThat(stringResponse).isEqualTo(CDSMockHelper.elBody);
                }
                else {
                    assertThat(stringResponse).isEqualTo(CDSMockHelper.esBody);
                }
            }
        };

        cdsHandler.fetchTranslations(null, null, callback);

        assertThat(callback.onFetchingTranslationsCalled).isTrue();
        assertThat(callback.onFetchingTranslationsCalled).isTrue();
        assertThat(callback.onFailureCalled).isFalse();

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = cdsMock.getServer().takeRequest();
        } catch (InterruptedException ignored) {
        }
        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer token");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");
        assertThat(recordedRequest.getHeader("x-native-sdk")).isEqualTo("mobile/android/" + BuildProperties.getSDKVersion());
        assertThat(recordedRequest.getHeader("Accept-version")).isEqualTo("v2");
    }

    @Test
    public void testFetchTranslationsCallback_onlyElInResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());

        DummyFetchCallback callback = new DummyFetchCallback() {

            @Override
            public void onFetchingTranslations(@NonNull String[] localeCodes) {
                super.onFetchingTranslations(localeCodes);
                assertThat(localeCodes).asList().containsExactly("el", "es");
            }

            @Override
            public void onTranslationFetched(@Nullable InputStream inputStream, @NonNull String localeCode, @Nullable Exception exception) {
                super.onTranslationFetched(inputStream, localeCode, exception);

                assertThat(localeCode).isAnyOf("el", "es");

                if (localeCode.equals("el")) {
                    assertThat(inputStream).isNotNull();
                    String stringResponse = null;
                    try {
                        stringResponse = Utils.readInputStream(inputStream);
                    } catch (IOException ignored) {
                    }
                    assertThat(stringResponse).isEqualTo(CDSMockHelper.elBody);
                } else {
                    assertThat(inputStream).isNull();
                    assertThat(exception).isNotNull();
                }
            }
        };

        cdsHandler.fetchTranslations(null, null, callback);

        assertThat(callback.onFetchingTranslationsCalled).isTrue();
        assertThat(callback.onFetchingTranslationsCalled).isTrue();
        assertThat(callback.onFailureCalled).isFalse();

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = cdsMock.getServer().takeRequest();
        } catch (InterruptedException ignored) {
        }
        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer token");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");
        assertThat(recordedRequest.getHeader("x-native-sdk")).isEqualTo("mobile/android/" + BuildProperties.getSDKVersion());
    }

    @Test
    public void testFetchTranslations_badURL() {
        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, "invalidHostURL");

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null, null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).isEmpty();
    }

    @Test
    public void testFetchTranslations_normalResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null, null);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = cdsMock.getServer().takeRequest();
        } catch (InterruptedException ignored) {
        }
        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer token");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");
        assertThat(recordedRequest.getHeader("x-native-sdk")).isEqualTo("mobile/android/" + BuildProperties.getSDKVersion());

        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el", "es");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");

        LocaleData.LocaleStrings esStrings = map.get("es");
        assertThat(esStrings).isNotNull();
        assertThat(esStrings.get("test_key")).isEqualTo("Buenos días");
        assertThat(esStrings.get("another_key")).isEqualTo("Buenas tardes");
        assertThat(esStrings.get("key3")).isEqualTo("");
    }

    @Test
    public void testFetchTranslations_onlyElInResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null, null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");
    }

    @Test
    public void testFetchTranslations_specifyLocale_normalResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations("el", null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");
    }

    @Test
    public void testFetchTranslations_specifyTags_normalResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEsWithTagsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());

        HashSet<String> tags = new HashSet<>(Arrays.asList("tag a", "tag b"));
        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null, tags);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el", "es");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");

        LocaleData.LocaleStrings esStrings = map.get("es");
        assertThat(esStrings).isNotNull();
        assertThat(esStrings.get("test_key")).isEqualTo("Buenos días");
        assertThat(esStrings.get("another_key")).isEqualTo("Buenas tardes");
        assertThat(esStrings.get("key3")).isEqualTo("");
    }

    @Test
    public void testFetchTranslations_first202_thenNormalResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEs202OnceDispatcher(10));

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null, null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el", "es");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");

        LocaleData.LocaleStrings esStrings = map.get("es");
        assertThat(esStrings).isNotNull();
        assertThat(esStrings.get("test_key")).isEqualTo("Buenos días");
        assertThat(esStrings.get("another_key")).isEqualTo("Buenas tardes");
        assertThat(esStrings.get("key3")).isEqualTo("");
    }

    @Test
    public void testFetchTranslations_only202() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEs202OnceDispatcher(30));

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null, null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).isEmpty();
    }

    @Test
    public void testFetchTranslations_onlyElInResponse_badJSONFormatting() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElDispatcherBadJsonFormatting());

        String[] localeCodes = new String[]{"el"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null, null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).isEmpty();
    }

    @Test
    public void testPushSourceStrings_badURL() {
        CDSHandler cdsHandler = new CDSHandler(null, "token", "secret", "invalidHostURL");

        LocaleData.TxPostData postData = getPostData();
        LocaleData.TxJobStatus jobStatus = null;
        try {
            jobStatus = cdsHandler.pushSourceStrings(postData);
        } catch (TimeLimitExceededException ignored) {}

        assertThat(jobStatus).isNull();
    }

    @Test
    public void testPushSourceStrings_normal() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getPostDispatcher());

        CDSHandler cdsHandler = new CDSHandler(null, "token", "secret", cdsMock.getBaseUrl());

        LocaleData.TxPostData postData = getPostData();
        LocaleData.TxJobStatus jobStatus = null;
        try {
            jobStatus = cdsHandler.pushSourceStrings(postData);
        } catch (TimeLimitExceededException ignored) {}

        assertThat(jobStatus).isNotNull();

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = cdsMock.getServer().takeRequest();
        } catch (InterruptedException ignored) {}
        assertThat(recordedRequest).isNotNull();

        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer token:secret");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");
        assertThat(recordedRequest.getHeader("x-native-sdk")).isEqualTo("mobile/android/" + BuildProperties.getSDKVersion());
        assertThat(recordedRequest.getHeader("Accept-version")).isEqualTo("v2");

        String postBody = recordedRequest.getBody().readUtf8();
        Gson gson = new Gson();
        LocaleData.TxPostData parsedPostData = gson.fromJson(postBody, LocaleData.TxPostData.class);
        assertThat(parsedPostData.data.keySet()).containsExactly("key1", "key2").inOrder();
        assertThat(parsedPostData.data.get("key1").string).isEqualTo("This is a source string.");
        assertThat(parsedPostData.data.get("key2").string).isEqualTo("Using special \ncharacters εδώ.");

        assertThat(parsedPostData.meta.purge).isTrue();
    }

    @Test
    public void testPushSourceStrings_CDSRespondsWith409_returnNull() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getPostWith409Dispatcher());

        CDSHandler cdsHandler = new CDSHandler(null, "token", "secret", cdsMock.getBaseUrl());

        LocaleData.TxPostData postData = getPostData();
        LocaleData.TxJobStatus jobStatus = null;
        try {
            jobStatus = cdsHandler.pushSourceStrings(postData);
        } catch (TimeLimitExceededException ignored) {}


        assertThat(jobStatus).isNull();
    }

    @Test
    public void testPushSourceStrings_CDSRespondsWithFailedJob_returnErrorInResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getPostWithFailedJobDispatcher());

        CDSHandler cdsHandler = new CDSHandler(null, "token", "secret", cdsMock.getBaseUrl());

        LocaleData.TxPostData postData = getPostData();
        LocaleData.TxJobStatus jobStatus = null;
        try {
            jobStatus = cdsHandler.pushSourceStrings(postData);
        } catch (TimeLimitExceededException ignored) {}

        assertThat(jobStatus).isNotNull();

        assertThat(jobStatus.data.errors).asList().hasSize(1);
        LocaleData.TxJobStatus.Data.Error error = jobStatus.data.errors[0];

        assertThat(error.status).isEqualTo(409);
        assertThat(error.code).isEqualTo("conflict");
        assertThat(error.title).isEqualTo("Conflict error");
        assertThat(error.detail).isEqualTo("Expected plural rules '['one', 'other']' instead got '['few', 'many', 'one', 'other', 'two', 'zero']' for new resource string on source language 'en'");
    }
}