package com.transifex.txnative.cache;

import android.os.Build;

import com.transifex.common.LocaleData;
import com.transifex.common.TranslationMapStorage;
import com.transifex.common.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class TxDiskTranslationsProviderTest {

    File tempDir = new File("build" + File.separator + "unitTestTempDir");

    @Before
    public void setUp() {
        if (tempDir.exists()) {
            Utils.deleteDirectory(tempDir);
        }
    }

    @After
    public void Teardown() {
        if (tempDir.exists()) {
            boolean deleted = Utils.deleteDirectory(tempDir);
            if (!deleted) {
                System.out.println("Could not delete tmp dir after test. Next test may fail.");
            }
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
    public void testGetTranslations_normal() {
        assertThat(tempDir.mkdirs()).isTrue();

        LocaleData.TranslationMap map = getElTranslationMap();
        TranslationMapStorage storage = new TranslationMapStorage(TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
        storage.toDisk(map, tempDir);

        TxDiskTranslationsProvider provider = new TxDiskTranslationsProvider(tempDir);

        assertThat(provider.getTranslations()).isEqualTo(map);
    }

    @Test
    public void testGetTranslations_dirDoesNotExist_returnNullTranslationMap() {
        TxDiskTranslationsProvider provider = new TxDiskTranslationsProvider(tempDir);

        assertThat(provider.getTranslations()).isNull();
    }

    @Test
    public void testGetTranslations_emptyDir_returnNullTranslationMap() {
        assertThat(tempDir.mkdirs()).isTrue();

        TxDiskTranslationsProvider provider = new TxDiskTranslationsProvider(tempDir);

        assertThat(provider.getTranslations()).isNull();
    }

}