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

import okhttp3.mockwebserver.RecordedRequest;

import static com.google.common.truth.Truth.assertThat;

public class TranslationsDownloaderTest {

    private CDSMockHelper cdsMock = null;
    TempDirHelper tempDir = null;

    @Before
    public void setUp() {
        cdsMock = new CDSMockHelper();
        cdsMock.setUpServer();

        tempDir = new TempDirHelper();
        tempDir.setUp();
    }

    @After
    public void Teardown() {
        if (cdsMock != null) {
            cdsMock.teardownServer();
        }

        if (tempDir != null) {
            tempDir.tearDown();
        }
    }

    @Test
    public void testSaveTranslations_dirDoesNotExist() {
        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir.getFile(), "strings.txt");

        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles).isEmpty();
    }

    @Test
    public void testSaveTranslations_normalResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEsDispatcher());

        boolean tempDirCreated =  tempDir.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir.getFile(), "strings.txt");

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = cdsMock.getServer().takeRequest();
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
        assertThat(elString).isEqualTo(CDSMockHelper.elBody);

        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(translationFiles.get("es")));
        } catch (IOException ignored) {}

        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(CDSMockHelper.esBody);
    }

    @Test
    public void testSaveTranslations_onlyElInResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElDispatcher());

        boolean tempDirCreated =  tempDir.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir.getFile(), "strings.txt");
        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles.keySet()).containsExactly("el");

        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(translationFiles.get("el")));
        } catch (IOException ignored) {}

        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSMockHelper.elBody);
    }

    @Test
    public void testSaveTranslations_specifyLocale_normalResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEsDispatcher());

        boolean tempDirCreated =  tempDir.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations("el", null, tempDir.getFile(), "strings.txt");
        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles.keySet()).containsExactly("el");

        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(translationFiles.get("el")));
        } catch (IOException ignored) {}

        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSMockHelper.elBody);
    }

    @Test
    public void testSaveTranslations_specifyTags_normalResponse() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElEsWithTagsDispatcher());

        boolean tempDirCreated =  tempDir.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        String[] localeCodes = new String[]{"el", "es"};
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        Set<String> tags = new HashSet<>(Arrays.asList("tag a", "tag b"));
        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, tags, tempDir.getFile(), "strings.txt");
        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles.keySet()).containsExactly("el", "es");

        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(translationFiles.get("el")));
        } catch (IOException ignored) {}

        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSMockHelper.elBody);

        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(translationFiles.get("es")));
        } catch (IOException ignored) {}

        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(CDSMockHelper.esBody);
    }

    @Test
    public void testSaveTranslations_overwriteExistingFile() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElDispatcher());

        File localeDir = new File(tempDir.getFile().getAbsoluteFile() + File.separator + "el");
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
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir.getFile(), "strings.txt");
        assertThat(translationFiles).isNotNull();
        assertThat(translationFiles.keySet()).containsExactly("el");

        String elString = null;
        try {
            elString = Utils.readInputStream(new FileInputStream(translationFiles.get("el")));
        } catch (IOException ignored) {}

        assertThat(elString).isNotNull();
        assertThat(elString).isEqualTo(CDSMockHelper.elBody);
    }

    @Test
    public void testSaveTranslations_skipExistingFileIfError() {
        cdsMock.getServer().setDispatcher(CDSMockHelper.getElDispatcher());

        File localeDir = new File(tempDir.getFile().getAbsoluteFile() + File.separator + "es");
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
        CDSHandler cdsHandler = new CDSHandler(localeCodes, "token", null, cdsMock.getBaseUrl());
        TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);

        HashMap<String, File> translationFiles = downloader.downloadTranslations(null, null, tempDir.getFile(), "strings.txt");
        assertThat(translationFiles).isEmpty();

        String esString = null;
        try {
            esString = Utils.readInputStream(new FileInputStream(dummyEsStringFile));
        } catch (IOException ignored) {}

        assertThat(esString).isNotNull();
        assertThat(esString).isEqualTo(dummyContent);
    }
}