package com.transifex.txnative.cache;

import android.content.Context;
import android.content.res.AssetManager;

import com.transifex.common.LocaleData;
import com.transifex.common.TranslationMapStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TxDiskTranslationsProviderAssetsTest {

    // This test, in contrast to the unit test found in the "test" folder, tests the AssetManager
    // version of TxDiskTranslationsProvider.

    // The tests rely on the following directory:
    //
    // androidTest/assets/txnative
    //
    // Note that aapt2 will remove empty directories when packing the assets. So there is no reason
    // to test for an empty directory.

    Context appContext = null;
    AssetManager assetManager;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assetManager = appContext.getAssets();
    }

    @Test
    public void testGetTranslations_translationsInAssets() {
        TxDiskTranslationsProvider provider = new TxDiskTranslationsProvider(assetManager, TranslationMapStorage.DEFAULT_TRANSLATIONS_DIR_NAME);
        LocaleData.TranslationMap map = provider.getTranslations();

        assertThat(map).isEqualTo(TxStandardCacheTest.getAssetsEquivalentTranslationMap());
    }

    @Test
    public void testGetTranslations_dirDoesNotExist_returnNullTranslationMap() {
        TxDiskTranslationsProvider provider = new TxDiskTranslationsProvider(assetManager, "wrongDir");

        assertThat(provider.getTranslations()).isNull();
    }
}