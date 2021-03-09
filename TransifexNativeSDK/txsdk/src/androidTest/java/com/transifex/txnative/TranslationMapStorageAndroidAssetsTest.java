package com.transifex.txnative;

import android.content.Context;
import android.content.res.AssetManager;

import com.transifex.common.LocaleData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TranslationMapStorageAndroidAssetsTest {

    // The tests rely on the following directories:
    //
    // androidTest/assets/test_normal
    // androidTest/assets/test_oneLocaleHasInvalidJson
    // androidTest/assets/test_oneLocaleFileIsMissing
    // androidTest/assets/test_fileInsteadLocaleDir
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
    public void testFromAssetsDirectory_dirDoesNotExist_returnNullTranslationMap() {
        TranslationMapStorageAndroid reader = new TranslationMapStorageAndroid(assetManager, "strings.txt");
        LocaleData.TranslationMap map = reader.fromAssetsDirectory("wrongDir");

        assertThat(map).isNull();
    }

    @Test
    public void testFromAssetsDirectory_haveFileWhereLocaleDirExpected_returnNullTranslationMap() {
        TranslationMapStorageAndroid reader = new TranslationMapStorageAndroid(assetManager, "strings.txt");
        LocaleData.TranslationMap map = reader.fromAssetsDirectory("test_fileInsteadLocaleDir");

        assertThat(map).isNull();
    }

    @Test
    public void testFromAssetsDirectory_normal_returnNormalTranslationMap() {
        TranslationMapStorageAndroid reader = new TranslationMapStorageAndroid(assetManager, "strings.txt");
        LocaleData.TranslationMap map = reader.fromAssetsDirectory("test_normal");

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
    public void testFromAssetsDirectory_oneLocaleHasInvalidJson_returnTranslationMapWithTheRestLocales() {
        TranslationMapStorageAndroid reader = new TranslationMapStorageAndroid(assetManager, "strings.txt");
        LocaleData.TranslationMap map = reader.fromAssetsDirectory("test_oneLocaleHasInvalidJson");

        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");
    }

    @Test
    public void testFromAssetsDirector_oneLocaleHasEmptyDir_getTranslationMapWithTheRestLocales() {
        TranslationMapStorageAndroid reader = new TranslationMapStorageAndroid(assetManager, "strings.txt");
        LocaleData.TranslationMap map = reader.fromAssetsDirectory("test_oneLocaleFileIsMissing");

        assertThat(map).isNotNull();
        assertThat(map.getLocales()).containsExactly("el");

        LocaleData.LocaleStrings elStrings = map.get("el");
        assertThat(elStrings).isNotNull();
        assertThat(elStrings.get("test_key")).isEqualTo("Καλημέρα");
        assertThat(elStrings.get("another_key")).isEqualTo("Καλό απόγευμα");
        assertThat(elStrings.get("key3")).isEqualTo("");
    }
}