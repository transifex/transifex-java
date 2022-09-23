package com.transifex.txnative.cache;

import android.os.Build;

import com.transifex.common.LocaleData;
import com.transifex.common.TempDirHelper;
import com.transifex.common.TranslationMapStorage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class TxDiskTranslationsProviderFilesTest {

    // Tests the File version of TxDiskTranslationsProvider.

    TempDirHelper tempDirHelper = null;

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

    private LocaleData.TranslationMap getElTranslationMap() {
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(1);
        elStrings.put("tx_test_key", new LocaleData.StringInfo("test ελ tx"));

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(1);
        translationMap.put("el", elStrings);

        return translationMap;
    }

    @Test
    public void testGetTranslations_normal() {
        assertThat(tempDirHelper.getFile().mkdirs()).isTrue();

        LocaleData.TranslationMap map = getElTranslationMap();
        TranslationMapStorage storage = new TranslationMapStorage(TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME);
        storage.toDisk(map, tempDirHelper.getFile());

        TxDiskTranslationsProvider provider = new TxDiskTranslationsProvider(tempDirHelper.getFile());

        assertThat(provider.getTranslations()).isEqualTo(map);
    }

    @Test
    public void testGetTranslations_dirDoesNotExist_returnNullTranslationMap() {
        TxDiskTranslationsProvider provider = new TxDiskTranslationsProvider(tempDirHelper.getFile());

        assertThat(provider.getTranslations()).isNull();
    }

    @Test
    public void testGetTranslations_emptyDir_returnNullTranslationMap() {
        assertThat(tempDirHelper.getFile().mkdirs()).isTrue();

        TxDiskTranslationsProvider provider = new TxDiskTranslationsProvider(tempDirHelper.getFile());

        assertThat(provider.getTranslations()).isNull();
    }

}