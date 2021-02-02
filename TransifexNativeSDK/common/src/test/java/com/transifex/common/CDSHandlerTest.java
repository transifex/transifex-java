package com.transifex.common;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import androidx.annotation.NonNull;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;

public class CDSHandlerTest {

    private MockWebServer server = null;
    private String baseUrl = null;

    @Before
    public void setUp() {
        server = new MockWebServer();
        baseUrl = server.url("").toString();
    }

    @After
    public void Teardown() {
        if (server != null) {
            try {
                server.shutdown();
            } catch (IOException ignored) {}
        }
    }

    private static final String elBody = "{\"data\":{\"test_key\":{\"string\":\"Καλημέρα\"},\"another_key\":{\"string\":\"Καλό απόγευμα\"},\"key3\":{\"string\":\"\"}}}";
    private  static final String esBody = "{\"data\":{\"test_key\":{\"string\":\"Buenos días\"},\"another_key\":{\"string\":\"Buenas tardes\"},\"key3\":{\"string\":\"\"}}}";
    private static final String elBodyBadFormatting = "not a JSON format";

    private Dispatcher getElEsDispatcher() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content/el":
                        return new MockResponse().setResponseCode(200).setBody(elBody);
                    case "/content/es":
                        return new MockResponse().setResponseCode(200).setBody(esBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    private Dispatcher getElDispatcher() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content/el":
                        return new MockResponse().setResponseCode(200).setBody(elBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    private Dispatcher getElDispatcherBadJsonFormatting() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content/el":
                        return new MockResponse().setResponseCode(200).setBody(elBodyBadFormatting);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    private Dispatcher getElEs202OnceDispatcher(final int maxRetryTimes) {

        Dispatcher dispatcher = new Dispatcher() {

            int elCounter = 0;
            int eSCounter = 0;

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content/el":
                        if (elCounter < maxRetryTimes) {
                            elCounter++;
                            return new MockResponse().setResponseCode(202);
                        }
                        return new MockResponse().setResponseCode(200).setBody(elBody);
                    case "/content/es":
                        if (eSCounter < maxRetryTimes) {
                            eSCounter++;
                            return new MockResponse().setResponseCode(202);
                        }
                        return new MockResponse().setResponseCode(200).setBody(esBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    private Dispatcher getPostDispatcher() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content":
                        return new MockResponse().setResponseCode(200).setBody(elBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    private LocaleData.TxPostData getPostData() {
        LinkedHashMap<String, LocaleData.StringInfo> data = new LinkedHashMap<>();
        data.put("key1", new LocaleData.StringInfo("This is a source string."));
        data.put("key2", new LocaleData.StringInfo("Using special \ncharacters εδώ."));

        LocaleData.TxPostData.Meta meta = new LocaleData.TxPostData.Meta();
        meta.purge = true;

        return new LocaleData.TxPostData(data, meta);
    }

    @Test
    public void testFetchTranslations_badURL() {
        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, "invalidHostURL");

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).isEmpty();
    }

    @Test
    public void testFetchTranslations_normalResponse() {
        server.setDispatcher(getElEsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest();
        } catch (InterruptedException ignored) {
        }
        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer token");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");

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
        server.setDispatcher(getElDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null);
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
        server.setDispatcher(getElEsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations("el");
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");
    }

    @Test
    public void testFetchTranslations_first202_thenNormalResponse() {
        server.setDispatcher(getElEs202OnceDispatcher(10));

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null);
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
    public void testFetchTranslations_Only202() {
        server.setDispatcher(getElEs202OnceDispatcher(30));

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).isEmpty();
    }

    @Test
    public void testFetchTranslations_onlyElInResponse_badJSONFormatting() {
        server.setDispatcher(getElDispatcherBadJsonFormatting());

        String[] localeCodes = new String[]{"el"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).isEmpty();
    }

    @Test
    public void testPostSourceStrings_badURL() {
        CDSHandler cdsHandler = new CDSHandler(null, "token", "secret", "invalidHostURL");

        LocaleData.TxPostData postData = getPostData();
        boolean success = cdsHandler.postSourceStrings(postData);

        assertThat(success).isFalse();
    }

    @Test
    public void testPostSourceStrings_normal() {
        server.setDispatcher(getPostDispatcher());

        CDSHandler cdsHandler = new CDSHandler(null, "token", "secret", baseUrl);

        LocaleData.TxPostData postData = getPostData();
        boolean success = cdsHandler.postSourceStrings(postData);

        assertThat(success).isTrue();

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest();
        } catch (InterruptedException ignored) {
        }
        assertThat(recordedRequest).isNotNull();

        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer token:secret");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");

        String postBody = recordedRequest.getBody().readUtf8();
        Gson gson = new Gson();
        LocaleData.TxPostData parsedPostData = gson.fromJson(postBody, LocaleData.TxPostData.class);
        assertThat(parsedPostData.data.keySet()).containsExactly("key1", "key2").inOrder();
        assertThat(parsedPostData.data.get("key1").string).isEqualTo("This is a source string.");
        assertThat(parsedPostData.data.get("key2").string).isEqualTo("Using special \ncharacters εδώ.");

        assertThat(parsedPostData.meta.purge).isTrue();
    }
}