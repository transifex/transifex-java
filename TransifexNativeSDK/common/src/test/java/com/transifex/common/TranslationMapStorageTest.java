package com.transifex.common;

import com.google.gson.Gson;

import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;

public class TranslationMapStorageTest {

    TempDirHelper tempDirHelper = null;
    Gson gson = new Gson();

    @Before
    public void setUp() {
        tempDirHelper = new TempDirHelper();
        tempDirHelper.setUp();
    }

    @After
    public void Teardown() {
        if (tempDirHelper != null) {
            tempDirHelper.tearDown();
        }
    }

    public LocaleData.LocaleStrings readLocaleStrings(File file) {
        LocaleData.LocaleStrings localeStrings = null;
        try {
            String string = Utils.readInputStream(new FileInputStream(file));
            LocaleData.TxPullResponseData data = gson.fromJson(string, LocaleData.TxPullResponseData.class);
            localeStrings = new LocaleData.LocaleStrings(data.data);
        } catch (Exception ignored) {}
        return localeStrings;
    }

    public boolean writeString(String string, File file) {
        boolean fileWritten = false;
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(string.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
            fileWritten = true;
        } catch (IOException ignored) {}

        return fileWritten;
    }

    @Test
    public void testToDisk_dirDoesNotExist_createDirAndWriteTranslationsNormally() {
        LocaleData.TranslationMap translationMap = StringTestData.getElEsTranslationMap();
        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        HashMap<String, File> files = storage.toDisk(translationMap, tempDirHelper.getFile());

        assertThat(files).isNotNull();
        assertThat(files.keySet()).containsExactly("el", "es");
        assertThat(files.get("el").getName()).isEqualTo("strings.txt");
        assertThat(files.get("es").getName()).isEqualTo("strings.txt");

        LocaleData.LocaleStrings elLocaleStrings = readLocaleStrings(files.get("el"));

        assertThat(elLocaleStrings).isNotNull();
        assertThat(elLocaleStrings).isEqualTo(translationMap.get("el"));

        LocaleData.LocaleStrings esLocaleStrings = readLocaleStrings(files.get("es"));

        assertThat(esLocaleStrings).isNotNull();
        assertThat(esLocaleStrings).isEqualTo(translationMap.get("es"));
    }

    @Test
    public void testToDisk_normal_writeTranslationsNormally() {
        boolean tempDirCreated =  tempDirHelper.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        LocaleData.TranslationMap translationMap = StringTestData.getElEsTranslationMap();
        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        HashMap<String, File> files = storage.toDisk(translationMap, tempDirHelper.getFile());

        assertThat(files).isNotNull();
        assertThat(files.keySet()).containsExactly("el", "es");
        assertThat(files.get("el").getName()).isEqualTo("strings.txt");
        assertThat(files.get("es").getName()).isEqualTo("strings.txt");

        // Check locale directory name and filename
        File elStringFile = files.get("el");
        assertThat(FilenameUtils.getName(elStringFile.getPath())).isEqualTo("strings.txt");
        assertThat(FilenameUtils.getName(elStringFile.getParent())).isEqualTo("el");

        LocaleData.LocaleStrings elLocaleStrings = readLocaleStrings(files.get("el"));

        assertThat(elLocaleStrings).isNotNull();
        assertThat(elLocaleStrings).isEqualTo(translationMap.get("el"));

        LocaleData.LocaleStrings esLocaleStrings = readLocaleStrings(files.get("es"));

        assertThat(esLocaleStrings).isNotNull();
        assertThat(esLocaleStrings).isEqualTo(translationMap.get("es"));
    }

    @Test
    public void testToDisk_emptyTranslationMap_returnEmptyFileMapAndNoLocaleFolderCreated() {
        boolean tempDirCreated =  tempDirHelper.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(0);
        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        HashMap<String, File> files = storage.toDisk(translationMap, tempDirHelper.getFile());

        assertThat(files).isNotNull();
        assertThat(files.keySet()).isEmpty();
        assertThat(tempDirHelper.getFile().list()).asList().isEmpty();
    }

    @Test
    public void testToDisk_translationFileExists_overwriteExistingFile() {
        // Create a pre-existing translation file for el
        File localeDir = new File(tempDirHelper.getFile().getAbsoluteFile() + File.separator + "el");
        boolean localeDirCreated =  localeDir.mkdirs();
        assertThat(localeDirCreated).isTrue();
        File dummyElStringFile = new File(localeDir + File.separator + "strings.txt");
        boolean dummyFileWritten = false;
        try {
            FileOutputStream outputStream = new FileOutputStream(dummyElStringFile);
            String dummyContent = "some text";
            outputStream.write(dummyContent.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
            dummyFileWritten = true;
        } catch (IOException ignored) {}
        assertThat(dummyFileWritten).isTrue();

        LocaleData.TranslationMap translationMap = StringTestData.getElTranslationMap();
        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        HashMap<String, File> files = storage.toDisk(translationMap, tempDirHelper.getFile());

        // Check that file is overwritten
        assertThat(files).isNotNull();
        assertThat(files.keySet()).containsExactly("el");
        boolean isSameFile = false;
        try {
            isSameFile = Files.isSameFile(dummyElStringFile.toPath(), files.get("el").toPath());
        } catch (IOException ignore) {
        }
        assertThat(isSameFile).isTrue();
        LocaleData.LocaleStrings elLocaleStrings = readLocaleStrings(files.get("el"));
        assertThat(elLocaleStrings).isNotNull();
        assertThat(elLocaleStrings).isEqualTo(translationMap.get("el"));
    }

    @Test
    public void testToDisk_translationFileExistsForNonSupportedLocale_keepExistingFile() {
        // Create a pre-existing translation file for de
        File localeDir = new File(tempDirHelper.getFile().getAbsoluteFile() + File.separator + "de");
        boolean localeDirCreated =  localeDir.mkdirs();
        assertThat(localeDirCreated).isTrue();
        File dummyDeStringFile = new File(localeDir + File.separator + "strings.txt");
        boolean dummyFileWritten = false;
        try {
            FileOutputStream outputStream = new FileOutputStream(dummyDeStringFile);
            String dummyContent = "some text";
            outputStream.write(dummyContent.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
            dummyFileWritten = true;
        } catch (IOException ignored) {}
        assertThat(dummyFileWritten).isTrue();

        LocaleData.TranslationMap translationMap = StringTestData.getElTranslationMap();
        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        HashMap<String, File> files = storage.toDisk(translationMap, tempDirHelper.getFile());

        // Check old "de" folder still exists and new "el" was created
        assertThat(tempDirHelper.getFile().list()).asList().containsExactly("el", "de");

        // Check old file for de still exists
        assertThat(dummyDeStringFile.exists()).isTrue();

        // Check new file for el contains correct data
        assertThat(files).isNotNull();
        assertThat(files.keySet()).containsExactly("el");
        LocaleData.LocaleStrings elLocaleStrings = readLocaleStrings(files.get("el"));
        assertThat(elLocaleStrings).isNotNull();
        assertThat(elLocaleStrings).isEqualTo(translationMap.get("el"));
    }

    @Test
    public void testFromDisk_dirDoesNotExist_returnNullTranslationMap() {
        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        LocaleData.TranslationMap map = storage.fromDisk(tempDirHelper.getFile());

        assertThat(map).isNull();
    }

    @Test
    public void testFromDisk_emptyDir_returnNullTranslationMap() {
        boolean tempDirCreated =  tempDirHelper.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        LocaleData.TranslationMap map = storage.fromDisk(tempDirHelper.getFile());

        assertThat(map).isNull();
    }

    @Test
    public void testFromDisk_haveFileWhereLocaleDirExpected_returnNullTranslationMap() {
        boolean tempDirCreated =  tempDirHelper.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        // Create a file where a locale folder is expected
        File fileInsteadOfLocaleDir = new File(tempDirHelper.getFile() + File.separator + "dummyFile");
        assertThat(writeString("dummy content", fileInsteadOfLocaleDir)).isTrue();

        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        LocaleData.TranslationMap map = storage.fromDisk(tempDirHelper.getFile());

        assertThat(map).isNull();
    }

    @Test
    public void testFromDisk_normal_returnNormalTranslationMap() {
        boolean tempDirCreated =  tempDirHelper.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        String filename = "strings.txt";

        File localeDir = new File(tempDirHelper.getFile() + File.separator + "el");
        assertThat(localeDir.mkdir()).isTrue();
        File localeFile = new File (localeDir + File.separator, filename);
        assertThat(writeString(CDSMockHelper.elBody, localeFile)).isTrue();

        localeDir = new File(tempDirHelper.getFile() + File.separator + "es");
        assertThat(localeDir.mkdir()).isTrue();
        localeFile = new File (localeDir + File.separator, filename);
        assertThat(writeString(CDSMockHelper.esBody, localeFile)).isTrue();

        TranslationMapStorage storage = new TranslationMapStorage(filename);
        LocaleData.TranslationMap map = storage.fromDisk(tempDirHelper.getFile());

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
    public void testFromDisk_oneLocaleHasInvalidJson_returnTranslationMapWithTheRestLocales() {
        boolean tempDirCreated =  tempDirHelper.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        String filename = "strings.txt";

        File localeDir = new File(tempDirHelper.getFile() + File.separator + "el");
        assertThat(localeDir.mkdir()).isTrue();
        File localeFile = new File (localeDir + File.separator, filename);
        assertThat(writeString(CDSMockHelper.elBody, localeFile)).isTrue();

        // Create a translation file containing invalid json syntax for es
        localeDir = new File(tempDirHelper.getFile() + File.separator + "es");
        assertThat(localeDir.mkdir()).isTrue();
        localeFile = new File (localeDir + File.separator, filename);
        assertThat(writeString("invalid json file", localeFile)).isTrue();

        TranslationMapStorage storage = new TranslationMapStorage(filename);
        LocaleData.TranslationMap map = storage.fromDisk(tempDirHelper.getFile());

        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");
    }

    @Test
    public void testFromDisk_oneLocaleHasEmptyDir_getTranslationMapWithTheRestLocales() {
        boolean tempDirCreated =  tempDirHelper.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        String filename = "strings.txt";

        File localeDir = new File(tempDirHelper.getFile() + File.separator + "el");
        assertThat(localeDir.mkdir()).isTrue();
        File localeFile = new File (localeDir + File.separator, filename);
        assertThat(writeString(CDSMockHelper.elBody, localeFile)).isTrue();

        // es locale dir will contain no translation file
        localeDir = new File(tempDirHelper.getFile() + File.separator + "es");
        assertThat(localeDir.mkdir()).isTrue();

        TranslationMapStorage storage = new TranslationMapStorage(filename);
        LocaleData.TranslationMap map = storage.fromDisk(tempDirHelper.getFile());

        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");
    }

    @Test
    // Check that we can read what was written
    public void testToDiskAndFromDisk_normal() {
        boolean tempDirCreated =  tempDirHelper.getFile().mkdirs();
        assertThat(tempDirCreated).isTrue();

        LocaleData.TranslationMap translationMap = StringTestData.getElEsTranslationMap();
        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        HashMap<String, File> files = storage.toDisk(translationMap, tempDirHelper.getFile());

        assertThat(files).isNotNull();
        assertThat(files.keySet()).containsExactly("el", "es");

        LocaleData.TranslationMap map = storage.fromDisk(tempDirHelper.getFile());
        assertThat(map).isNotNull();
        assertThat(map).isEqualTo(translationMap);
    }
}