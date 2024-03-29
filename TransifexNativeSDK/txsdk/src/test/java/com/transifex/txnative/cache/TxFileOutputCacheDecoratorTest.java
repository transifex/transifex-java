package com.transifex.txnative.cache;

import com.google.common.util.concurrent.MoreExecutors;
import com.transifex.common.LocaleData;
import com.transifex.common.TempDirHelper;
import com.transifex.common.TranslationMapStorage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;

public class TxFileOutputCacheDecoratorTest {

    TempDirHelper tempDir = null;

    @Before
    public void setUp() {
        tempDir = new TempDirHelper();
        tempDir.setUp();
    }

    @After
    public void Teardown() {
        if (tempDir != null) {
            tempDir.tearDown();
        }
    }

    private LocaleData.TranslationMap getElTranslationMap() {
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(1);
        elStrings.put("tx_test_key", new LocaleData.StringInfo("test ελ tx"));

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(1);
        translationMap.put("el", elStrings);

        return translationMap;
    }

    @Test
    public void testUpdate_normal_writeTranslations() {
        assertThat(tempDir.getFile().mkdirs()).isTrue();

        TxMemoryCache internalCache = new TxMemoryCache();
        TxFileOutputCacheDecorator fileOutputCache = new TxFileOutputCacheDecorator(
                MoreExecutors.directExecutor(), tempDir.getFile(), internalCache);
        LocaleData.TranslationMap map = getElTranslationMap();
        fileOutputCache.update(map);

        // Check that internal cache is updated
        assertThat(fileOutputCache.get()).isEqualTo(map);

        // Check that the translations on disk are correct
        TranslationMapStorage storage = new TranslationMapStorage(TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
        File elStringFile = new File(tempDir.getFile().getPath() + File.separator + "el"
                + File.separator + TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
        assertThat(elStringFile.exists()).isTrue();
        LocaleData.TranslationMap readMap = storage.fromDisk(tempDir.getFile());
        assertThat(readMap).isEqualTo(map);
    }

    @Test
    public void testUpdate_translationFileExistsForNonSupportedLocale_fileIsDeleted() {
        // Create a pre-existing translation file for de
        File localeDir = new File(tempDir.getFile().getAbsoluteFile() + File.separator + "de");
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

        TxMemoryCache internalCache = new TxMemoryCache();
        TxFileOutputCacheDecorator fileOutputCache = new TxFileOutputCacheDecorator(
                MoreExecutors.directExecutor(), tempDir.getFile(), internalCache);
        LocaleData.TranslationMap map = getElTranslationMap();
        fileOutputCache.update(map);

        // Check that previous de folder is deleted and new el is created
        assertThat(tempDir.getFile().list()).asList().containsExactly("el");

        // Check that old file doesn't exist
        assertThat(dummyDeStringFile.exists()).isFalse();

        // Check that internal cache is updated
        assertThat(fileOutputCache.get()).isEqualTo(map);

        // Check that the translations on disk are correct
        TranslationMapStorage storage = new TranslationMapStorage(TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
        File elStringFile = new File(tempDir.getFile().getPath() + File.separator + "el"
                + File.separator + TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
        assertThat(elStringFile.exists()).isTrue();
        LocaleData.TranslationMap readMap = storage.fromDisk(tempDir.getFile());
        assertThat(readMap).isEqualTo(map);
    }

}