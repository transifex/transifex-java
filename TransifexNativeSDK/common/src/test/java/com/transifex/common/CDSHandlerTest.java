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

    //region CDS mock methods

    //TODO: The following code should be added in a testFixture once implemented: https://issuetracker.google.com/issues/139762443

    public static final String elBody = "{\"data\":{\"test_key\":{\"string\":\"Καλημέρα\"},\"another_key\":{\"string\":\"Καλό απόγευμα\"},\"key3\":{\"string\":\"\"}}}";
    public static final String esBody = "{\"data\":{\"test_key\":{\"string\":\"Buenos días\"},\"another_key\":{\"string\":\"Buenas tardes\"},\"key3\":{\"string\":\"\"}}}";
    public static final String elBodyBadFormatting = "not a JSON format";

    public static Dispatcher getElEsDispatcher() {
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

    public static Dispatcher getElDispatcher() {
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

    public static Dispatcher getElEsWithTagsDispatcher() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    // "/content/el?filter[tags]=tag a,tag b"
                    case "/content/el?filter%5Btags%5D=tag%20a%2Ctag%20b":
                        return new MockResponse().setResponseCode(200).setBody(elBody);
                    case "/content/es?filter%5Btags%5D=tag%20a%2Ctag%20b":
                        return new MockResponse().setResponseCode(200).setBody(esBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    public static Dispatcher getElDispatcherBadJsonFormatting() {
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

    public static Dispatcher getElEs202OnceDispatcher(final int maxRetryTimes) {

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
    // A dispatcher that emulates the "push content" and "job status" CDS endpoints. The "job status"
    // endpoint responds with "processing", followed by a "completed" job status response.
    public static Dispatcher getPostDispatcher() {

        Dispatcher dispatcher = new Dispatcher() {

            int counter = 0;

            final int processingCount = 1;
            final String jobId = "abcd";
            final String jobLink = "/jobs/content/" + jobId;
            final Gson gson = new Gson();

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content":
                        LocaleData.TxPostResponseData responseData = new LocaleData.TxPostResponseData();
                        responseData.data = new LocaleData.TxPostResponseData.Data();
                        responseData.data.id = jobId;
                        responseData.data.links = new LocaleData.TxPostResponseData.Data.Links();
                        responseData.data.links.job = jobLink;
                        return new MockResponse().setResponseCode(202).setBody(gson.toJson(responseData));
                    case jobLink:
                        if (counter < processingCount) {
                            counter++;
                            LocaleData.TxJobStatus jobStatus = new LocaleData.TxJobStatus();
                            jobStatus.data = new LocaleData.TxJobStatus.Data();
                            jobStatus.data.status = "processing";
                            return new MockResponse().setResponseCode(200).setBody(gson.toJson(jobStatus));
                        }
                        LocaleData.TxJobStatus jobStatus = new LocaleData.TxJobStatus();
                        jobStatus.data = new LocaleData.TxJobStatus.Data();
                        jobStatus.data.status = "completed";
                        jobStatus.data.details = new LocaleData.TxJobStatus.Data.Details();
                        jobStatus.data.details.created = 2;
                        return new MockResponse().setResponseCode(200).setBody(gson.toJson(jobStatus));
                }

                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    // A dispatcher that emulates the push content and Job status CDS endpoints. The job status
    // returned is "failed" and contains errors.
    public static Dispatcher getPostWithFailedJobDispatcher() {

        Dispatcher dispatcher = new Dispatcher() {

            final String jobId = "abcd";
            final String jobLink = "/jobs/content/" + jobId;
            final Gson gson = new Gson();

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content":
                        LocaleData.TxPostResponseData responseData = new LocaleData.TxPostResponseData();
                        responseData.data = new LocaleData.TxPostResponseData.Data();
                        responseData.data.id = jobId;
                        responseData.data.links = new LocaleData.TxPostResponseData.Data.Links();
                        responseData.data.links.job = jobLink;
                        return new MockResponse().setResponseCode(202).setBody(gson.toJson(responseData));
                    case jobLink:
                        LocaleData.TxJobStatus jobStatus = new LocaleData.TxJobStatus();
                        jobStatus.data = new LocaleData.TxJobStatus.Data();
                        jobStatus.data.status = "failed";
                        jobStatus.data.details = new LocaleData.TxJobStatus.Data.Details();
                        LocaleData.TxJobStatus.Data.Error error = new LocaleData.TxJobStatus.Data.Error();
                        error.status = 409;
                        error.code = "conflict";
                        error.title = "Conflict error";
                        error.detail = "Expected plural rules '['one', 'other']' instead got '['few', 'many', 'one', 'other', 'two', 'zero']' for new resource string on source language 'en'";
                        jobStatus.data.errors = new LocaleData.TxJobStatus.Data.Error[]{error};
                        return new MockResponse().setResponseCode(200).setBody(gson.toJson(jobStatus));
                }

                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    // A dispatcher that emulates the push content endpoints that returns 409.
    public static Dispatcher getPostWith409Dispatcher() {

        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content":
                        return new MockResponse().setResponseCode(409);
                }

                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    //endregion

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
        server.setDispatcher(getElEsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

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
                    assertThat(stringResponse).isEqualTo(elBody);
                }
                else {
                    assertThat(stringResponse).isEqualTo(esBody);
                }
            }
        };

        cdsHandler.fetchTranslations(null, null, callback);

        assertThat(callback.onFetchingTranslationsCalled).isTrue();
        assertThat(callback.onFetchingTranslationsCalled).isTrue();
        assertThat(callback.onFailureCalled).isFalse();

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest();
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
        server.setDispatcher(getElDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

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
                    assertThat(stringResponse).isEqualTo(elBody);
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
            recordedRequest = server.takeRequest();
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
        server.setDispatcher(getElEsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null, null);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest();
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
        server.setDispatcher(getElDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

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
        server.setDispatcher(getElEsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

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
        server.setDispatcher(getElEsWithTagsDispatcher());

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

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
        server.setDispatcher(getElEs202OnceDispatcher(10));

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

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
        server.setDispatcher(getElEs202OnceDispatcher(30));

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

        LocaleData.TranslationMap map = cdsHandler.fetchTranslations(null, null);
        assertThat(map).isNotNull();
        assertThat(map.getLocales()).isEmpty();
    }

    @Test
    public void testFetchTranslations_onlyElInResponse_badJSONFormatting() {
        server.setDispatcher(getElDispatcherBadJsonFormatting());

        String[] localeCodes = new String[]{"el"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);

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
        server.setDispatcher(getPostDispatcher());

        CDSHandler cdsHandler = new CDSHandler(null, "token", "secret", baseUrl);

        LocaleData.TxPostData postData = getPostData();
        LocaleData.TxJobStatus jobStatus = null;
        try {
            jobStatus = cdsHandler.pushSourceStrings(postData);
        } catch (TimeLimitExceededException ignored) {}

        assertThat(jobStatus).isNotNull();

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest();
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
        server.setDispatcher(getPostWith409Dispatcher());

        CDSHandler cdsHandler = new CDSHandler(null, "token", "secret", baseUrl);

        LocaleData.TxPostData postData = getPostData();
        LocaleData.TxJobStatus jobStatus = null;
        try {
            jobStatus = cdsHandler.pushSourceStrings(postData);
        } catch (TimeLimitExceededException ignored) {}


        assertThat(jobStatus).isNull();
    }

    @Test
    public void testPushSourceStrings_CDSRespondsWithFailedJob_returnErrorInResponse() {
        server.setDispatcher(getPostWithFailedJobDispatcher());

        CDSHandler cdsHandler = new CDSHandler(null, "token", "secret", baseUrl);

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