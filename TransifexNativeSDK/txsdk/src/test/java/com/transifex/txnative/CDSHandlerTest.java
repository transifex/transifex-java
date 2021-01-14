package com.transifex.txnative;

import android.os.Build;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class CDSHandlerTest {

    private MockWebServer server = null;

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

    private Dispatcher getElEsDispatcher() {
        Dispatcher dispatcher = new Dispatcher() {

            @NotNull
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

            @NotNull
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

    private Dispatcher getElEs202OnceDispatcher(final int maxRetryTimes) {

        Dispatcher dispatcher = new Dispatcher() {

            int elCounter = 0;
            int eSCounter = 0;

            @NotNull
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

    @Test
    public void testFetchTranslations_badURL() {
        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, "wrongurl");

        LocaleData.TranslationMap map = cdsHandler.fetchTranslationsInternal(null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).isEmpty();
    }

    @Test
    public void testFetchTranslations_normalResponse() {
        server = new MockWebServer();
        server.setDispatcher(getElEsDispatcher());
        String baseUrl = server.url("").toString();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslationsInternal(null);
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
        server = new MockWebServer();
        server.setDispatcher(getElDispatcher());
        String baseUrl = server.url("").toString();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslationsInternal(null);
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
        server = new MockWebServer();
        server.setDispatcher(getElEsDispatcher());
        String baseUrl = server.url("").toString();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslationsInternal("el");
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
        server = new MockWebServer();
        server.setDispatcher(getElEs202OnceDispatcher(10));
        String baseUrl = server.url("").toString();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslationsInternal(null);
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
    public void testFetchTranslations_202Only() {
        server = new MockWebServer();
        server.setDispatcher(getElEs202OnceDispatcher(30));
        String baseUrl = server.url("").toString();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslationsInternal(null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).isEmpty();
    }

}