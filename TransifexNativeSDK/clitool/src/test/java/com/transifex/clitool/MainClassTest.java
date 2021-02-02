package com.transifex.clitool;

import com.google.gson.Gson;
import com.transifex.common.LocaleData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;

public class MainClassTest {

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

    private Dispatcher getPostDispatcher() {
        Dispatcher dispatcher = new Dispatcher() {

            @NonNull
            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/content":
                        return new MockResponse().setResponseCode(200);
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        return dispatcher;
    }

    @Test
    public void testMain_noCommand() {
        String args = "-t token -s secret";
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(2);
    }

    @Test
    public void testPush_noSecret() {
        String args = "-t token push -m app";
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(2);
    }

    @Test
    public void testPush_wrongModuleName() {
        String args = "-t token -s secret push -m app2";
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(1);
    }


    @Test
    public void testPush_useFile() {
        // This test relies on having the following file:
        // txsdk/src/debug/res/values/strings.xml

        server.setDispatcher(getPostDispatcher());

        String args = String.format("-t token -s secret -u %s push -f %s", baseUrl,
                "../txsdk/src/debug/res/values/strings.xml");
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest();
        } catch (InterruptedException ignored) {
        }
        assertThat(recordedRequest).isNotNull();

        String postBody = recordedRequest.getBody().readUtf8();
        Gson gson = new Gson();
        LocaleData.TxPostData parsedPostData = gson.fromJson(postBody, LocaleData.TxPostData.class);
        assertThat(parsedPostData.data.keySet()).containsExactly("tx_test_key");
        assertThat(parsedPostData.data.get("tx_test_key").string).isEqualTo("test");
    }

    @Test
    public void testPush_useMultipleFiles() {
        // This test relies on having the following files:
        // txsdk/src/debug/res/values/strings.xml
        // txsdk/src/debug/res/values-el/strings.xml"

        server.setDispatcher(getPostDispatcher());

        String args = String.format("-t token -s secret -u %s push -f %s -f %s", baseUrl,
                "../txsdk/src/debug/res/values/strings.xml",
                "../txsdk/src/debug/res/values-el/strings.xml");
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest();
        } catch (InterruptedException ignored) {
        }
        assertThat(recordedRequest).isNotNull();

        String postBody = recordedRequest.getBody().readUtf8();
        Gson gson = new Gson();
        LocaleData.TxPostData parsedPostData = gson.fromJson(postBody, LocaleData.TxPostData.class);
        assertThat(parsedPostData.data.keySet()).containsExactly("tx_test_key");
        assertThat(parsedPostData.data.get("tx_test_key").string).isEqualTo("test ελ");
    }

    @Test
    public void testClear() {
        server.setDispatcher(getPostDispatcher());

        String args = String.format("-t token -s secret -u %s clear", baseUrl);
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest();
        } catch (InterruptedException ignored) {
        }
        assertThat(recordedRequest).isNotNull();

        String postBody = recordedRequest.getBody().readUtf8();
        Gson gson = new Gson();
        LocaleData.TxPostData parsedPostData = gson.fromJson(postBody, LocaleData.TxPostData.class);
        assertThat(parsedPostData.meta.purge).isTrue();
        assertThat(parsedPostData.data).isEmpty();
    }
}