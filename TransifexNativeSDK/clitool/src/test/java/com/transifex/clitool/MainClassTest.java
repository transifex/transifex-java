package com.transifex.clitool;

import com.google.gson.Gson;
import com.transifex.common.LocaleData;
import com.transifex.common.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import androidx.annotation.NonNull;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;

public class MainClassTest {

    private MockWebServer server = null;
    private String baseUrl = null;
    File tempDir = new File("build" + File.separator + "unitTestTempDir");

    @Before
    public void setUp() {
        server = new MockWebServer();
        baseUrl = server.url("").toString();

        if (tempDir.exists()) {
            Utils.deleteDirectory(tempDir);
        }
    }

    @After
    public void Teardown() {
        if (server != null) {
            try {
                server.shutdown();
            } catch (IOException ignored) {}
        }

        if (tempDir.exists()) {
            boolean deleted = Utils.deleteDirectory(tempDir);
            if (!deleted) {
                System.out.println("Could not delete tmp dir after test. Next test may fail.");
            }
        }
    }

    public static final String elBody = "{\"data\":{\"test_key\":{\"string\":\"Καλημέρα\"},\"another_key\":{\"string\":\"Καλό απόγευμα\"},\"key3\":{\"string\":\"\"}}}";
    public static final String esBody = "{\"data\":{\"test_key\":{\"string\":\"Buenos días\"},\"another_key\":{\"string\":\"Buenas tardes\"},\"key3\":{\"string\":\"\"}}}";

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

    public static Dispatcher getPostDispatcher() {

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

    @Test
    public void testMain_noCommand() {
        String args = "-t token -s secret";
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(2);
    }

    @Test
    public void testPush_noSecret() {
        String args = "push -t token -m app";
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(2);
    }

    @Test
    public void testPush_wrongModuleName() {
        String args = "push -t token -s secret -m app2";
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(1);
    }

    @Test
    public void testPush_useFile() {
        // This test relies on having the following file:
        // txsdk/src/test/res/values/strings.xml

        server.setDispatcher(getPostDispatcher());

        String args = String.format("-u %s push -t token -s secret -f %s -a %s -a %s", baseUrl,
                "../txsdk/src/test/res/values/strings.xml", "some_tag", "another_tag");
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
        assertThat(parsedPostData.data.keySet()).containsExactly("tx_test_key", "tx_plural_test_key").inOrder();
        assertThat(parsedPostData.data.get("tx_test_key").string).isEqualTo("test");
        assertThat(parsedPostData.data.get("tx_plural_test_key").string).isEqualTo("{cnt, plural, one {car} two {car 2} other {cars}}");

        // Check appended tags
        assertThat(parsedPostData.data.get("tx_test_key").meta.tags).isNotNull();
        assertThat(parsedPostData.data.get("tx_test_key").meta.tags).containsExactly("some_tag", "another_tag");
        assertThat(parsedPostData.data.get("tx_plural_test_key").meta.tags).containsExactly("some_tag", "another_tag");
    }

    @Test
    public void testPush_useMultipleFiles() {
        // This test relies on having the following files:
        // txsdk/src/test/res/values/strings.xml
        // txsdk/src/test/res/values-el/strings.xml"

        // The second file contains the same keys as the first file but with a different values. We
        // expect to see the last file's value in the pushed strings.

        server.setDispatcher(getPostDispatcher());

        String args = String.format("-u %s push -t token -s secret -f %s -f %s", baseUrl,
                "../txsdk/src/test/res/values/strings.xml",
                "../txsdk/src/test/res/values-el/strings.xml");
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
        assertThat(parsedPostData.data.keySet()).containsExactly("tx_test_key", "tx_plural_test_key").inOrder();
        assertThat(parsedPostData.data.get("tx_test_key").string).isEqualTo("test ελ");
        assertThat(parsedPostData.data.get("tx_plural_test_key").string).isEqualTo("{cnt, plural, one {αυτοκίνητο} other {αυτοκίνητα}}");
    }

    @Test
    public void testClear() {
        server.setDispatcher(getPostDispatcher());

        String args = String.format("-u %s clear -t token -s secret", baseUrl);
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

    @Test
    public void testPull_normal() {
        server.setDispatcher(getElEsDispatcher());

        String args = String.format("-u %s pull -t token -l el es -d %s", baseUrl, tempDir.getPath());
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        File elFile = Paths.get(tempDir.getPath(), MainClass.OUT_DIR_NAME, "el", MainClass.OUT_FILE_NAME).toFile();
        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(elFile));
        } catch (IOException ignored) {}
        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(elBody);

        File esFile = Paths.get(tempDir.getPath(), MainClass.OUT_DIR_NAME, "es", MainClass.OUT_FILE_NAME).toFile();
        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(esFile));
        } catch (IOException ignored) {}
        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(esBody);
    }

    @Test
    public void testPull_consecutiveRuns() {
        server.setDispatcher(getElEsDispatcher());

        String args = String.format("-u %s pull -t token -l el -d %s", baseUrl, tempDir.getPath());
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        args = String.format("-u %s pull -t token -l es -d %s", baseUrl, tempDir.getPath());
        returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        File elFile = Paths.get(tempDir.getPath(), MainClass.OUT_DIR_NAME, "el", MainClass.OUT_FILE_NAME).toFile();
        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(elFile));
        } catch (IOException ignored) {}
        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(elBody);

        File esFile = Paths.get(tempDir.getPath(), MainClass.OUT_DIR_NAME, "es", MainClass.OUT_FILE_NAME).toFile();
        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(esFile));
        } catch (IOException ignored) {}
        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(esBody);
    }

    @Test
    public void testPull_onlyElInResponse() {
        server.setDispatcher(getElDispatcher());

        String args = String.format("-u %s pull -t token -l el es -d %s", baseUrl, tempDir.getPath());
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(1);

        File elFile = Paths.get(tempDir.getPath(), MainClass.OUT_DIR_NAME, "el", MainClass.OUT_FILE_NAME).toFile();
        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(elFile));
        } catch (IOException ignored) {}
        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(elBody);

        File esFile = Paths.get(tempDir.getPath(), MainClass.OUT_DIR_NAME, "es", MainClass.OUT_FILE_NAME).toFile();
        assertThat(esFile.exists()).isFalse();
    }
}