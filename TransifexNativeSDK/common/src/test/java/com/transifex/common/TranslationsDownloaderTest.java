package com.transifex.common;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;

public class TranslationsDownloaderTest {

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

    @Test
    public void testSaveTranslations_dirDoesNotExist() {
        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir, "strings.txt");

        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles).isEmpty();
    }

    @Test
    public void testSaveTranslations_normalResponse() {
        server.setDispatcher(CDSHandlerTest.getElEsDispatcher());

        boolean tempDirCreated =  tempDir.mkdirs();
        assertThat(tempDirCreated).isTrue();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir, "strings.txt");

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest();
        } catch (InterruptedException ignored) {
        }
        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer token");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");

        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles.keySet()).containsExactly("el", "es");
        assertThat(translationFiles.get("el").getName()).isEqualTo("strings.txt");
        assertThat(translationFiles.get("es").getName()).isEqualTo("strings.txt");

        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(translationFiles.get("el")));
        } catch (IOException ignored) {}

        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSHandlerTest.elBody);

        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(translationFiles.get("es")));
        } catch (IOException ignored) {}

        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(CDSHandlerTest.esBody);
    }

    @Test
    public void testSaveTranslations_onlyElInResponse() {
        server.setDispatcher(CDSHandlerTest.getElDispatcher());

        boolean tempDirCreated =  tempDir.mkdirs();
        assertThat(tempDirCreated).isTrue();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir, "strings.txt");
        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles.keySet()).containsExactly("el");

        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(translationFiles.get("el")));
        } catch (IOException ignored) {}

        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSHandlerTest.elBody);
    }

    @Test
    public void testSaveTranslations_specifyLocale_normalResponse() {
        server.setDispatcher(CDSHandlerTest.getElEsDispatcher());

        boolean tempDirCreated =  tempDir.mkdirs();
        assertThat(tempDirCreated).isTrue();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations("el", null, tempDir, "strings.txt");
        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles.keySet()).containsExactly("el");

        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(translationFiles.get("el")));
        } catch (IOException ignored) {}

        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSHandlerTest.elBody);
    }

    @Test
    public void testSaveTranslations_specifyTags_normalResponse() {
        server.setDispatcher(CDSHandlerTest.getElEsWithTagsDispatcher());

        boolean tempDirCreated =  tempDir.mkdirs();
        assertThat(tempDirCreated).isTrue();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        Set<String> tags = new HashSet<>(Arrays.asList("tag a", "tag b"));
        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, tags, tempDir, "strings.txt");
        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles.keySet()).containsExactly("el", "es");

        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(translationFiles.get("el")));
        } catch (IOException ignored) {}

        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSHandlerTest.elBody);

        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(translationFiles.get("es")));
        } catch (IOException ignored) {}

        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(CDSHandlerTest.esBody);
    }

    @Test
    public void testSaveTranslations_overwriteExistingFile() {
        server.setDispatcher(CDSHandlerTest.getElDispatcher());

        File localeDir = new File(tempDir.getAbsoluteFile() + File.separator + "el");
        boolean localeDirCreated =  localeDir.mkdirs();
        assertThat(localeDirCreated).isTrue();

        File dummyElStringFile = new File(localeDir + File.separator + "strings.txt");
        try {
            FileOutputStream outputStream = new FileOutputStream(dummyElStringFile);
            String dummyContent = "some text";
            outputStream.write(dummyContent.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (IOException ignored) {}

        String[] localeCodes = new String[]{"el"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir, "strings.txt");
        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles.keySet()).containsExactly("el");

        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(translationFiles.get("el")));
        } catch (IOException ignored) {}

        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSHandlerTest.elBody);
    }

    @Test
    public void testSaveTranslations_skipExistingFileIfError() {
        server.setDispatcher(CDSHandlerTest.getElDispatcher());

        File localeDir = new File(tempDir.getAbsoluteFile() + File.separator + "es");
        boolean localeDirCreated =  localeDir.mkdirs();
        assertThat(localeDirCreated).isTrue();

        File dummyEsStringFile = new File(localeDir + File.separator + "strings.txt");
        String dummyContent = "some text";
        try {
            FileOutputStream outputStream = new FileOutputStream(dummyEsStringFile);
            outputStream.write(dummyContent.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (IOException ignored) {}

        String[] localeCodes = new String[]{"es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, baseUrl);
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir, "strings.txt");
        assertThat(translationFiles).isEmpty();

        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(dummyEsStringFile));
        } catch (IOException ignored) {}

        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(dummyContent);
    }
}