package com.transifex.clitool;

import com.google.gson.Gson;
import com.transifex.common.CDSMockHelper;
import com.transifex.common.LocaleData;
import com.transifex.common.TempDirHelper;
import com.transifex.common.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;

public class MainClassTest {

    // The tests rely on the following directories:
    //
    // txsdk/src/test/res/values
    // txsdk/src/test/res/values-el
    // txsdk/src/test/res/values-es

    private CDSMockHelper cdsMock = null;
    TempDirHelper tempDirHelper = null;

    @Before
    public void setUp() {
        cdsMock = new CDSMockHelper();
        cdsMock.setUpServer();

        tempDirHelper = new TempDirHelper();
        tempDirHelper.setUp();
    }

    @After
    public void Teardown() {
        if (cdsMock != null) {
            cdsMock.teardownServer();
        }

        if (tempDirHelper != null) {
            tempDirHelper.tearDown();
        }
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

        cdsMock.getServer().setDispatcher(CDSMockHelper.getPostDispatcher());

        String args = String.format("-u %s push -t token -s secret -f %s -a %s -a %s", cdsMock.getBaseUrl(),
                "../txsdk/src/test/res/values/strings.xml", "some_tag", "another_tag");
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = cdsMock.getServer().takeRequest();
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

        cdsMock.getServer().setDispatcher(CDSMockHelper.getPostDispatcher());

        String args = String.format("-u %s push -t token -s secret -f %s -f %s", cdsMock.getBaseUrl(),
                "../txsdk/src/test/res/values/strings.xml",
                "../txsdk/src/test/res/values-el/strings.xml");
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = cdsMock.getServer().takeRequest();
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
        cdsMock.getServer().setDispatcher(CDSMockHelper.getPostDispatcher());

        String args = String.format("-u %s clear -t token -s secret", cdsMock.getBaseUrl());
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = cdsMock.getServer().takeRequest();
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
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEsDispatcher());

        String args = String.format("-u %s pull -t token -l el es -d %s", cdsMock.getBaseUrl(), tempDirHelper.getFile().getPath());
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        File elFile = Paths.get(tempDirHelper.getFile().getPath(), MainClass.OUT_DIR_NAME, "el", MainClass.OUT_FILE_NAME).toFile();
        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(elFile));
        } catch (IOException ignored) {}
        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSMockHelper.elBody);

        File esFile = Paths.get(tempDirHelper.getFile().getPath(), MainClass.OUT_DIR_NAME, "es", MainClass.OUT_FILE_NAME).toFile();
        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(esFile));
        } catch (IOException ignored) {}
        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(CDSMockHelper.esBody);
    }

    @Test
    public void testPull_consecutiveRuns() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEsDispatcher());

        String args = String.format("-u %s pull -t token -l el -d %s", cdsMock.getBaseUrl(), tempDirHelper.getFile().getPath());
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        args = String.format("-u %s pull -t token -l es -d %s", cdsMock.getBaseUrl(), tempDirHelper.getFile().getPath());
        returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(0);

        File elFile = Paths.get(tempDirHelper.getFile().getPath(), MainClass.OUT_DIR_NAME, "el", MainClass.OUT_FILE_NAME).toFile();
        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(elFile));
        } catch (IOException ignored) {}
        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSMockHelper.elBody);

        File esFile = Paths.get(tempDirHelper.getFile().getPath(), MainClass.OUT_DIR_NAME, "es", MainClass.OUT_FILE_NAME).toFile();
        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(esFile));
        } catch (IOException ignored) {}
        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(CDSMockHelper.esBody);
    }

    @Test
    public void testPull_onlyElInResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElDispatcher());

        String args = String.format("-u %s pull -t token -l el es -d %s", cdsMock.getBaseUrl(), tempDirHelper.getFile().getPath());
        int returnValue = MainClass.testMain(args);

        assertThat(returnValue).isEqualTo(1);

        File elFile = Paths.get(tempDirHelper.getFile().getPath(), MainClass.OUT_DIR_NAME, "el", MainClass.OUT_FILE_NAME).toFile();
        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(elFile));
        } catch (IOException ignored) {}
        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSMockHelper.elBody);

        File esFile = Paths.get(tempDirHelper.getFile().getPath(), MainClass.OUT_DIR_NAME, "es", MainClass.OUT_FILE_NAME).toFile();
        assertThat(esFile.exists()).isFalse();
    }
}