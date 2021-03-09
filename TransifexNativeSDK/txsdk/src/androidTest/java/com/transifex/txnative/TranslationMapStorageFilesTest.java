package com.transifex.txnative;

import android.content.Context;

import com.transifex.common.LocaleData;
import com.transifex.common.TranslationMapStorage;
import com.transifex.common.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;

import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TranslationMapStorageFilesTest {

    Context appContext = null;
    File tempDir;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File filesDir = appContext.getFilesDir();
        tempDir = new File(filesDir.getPath() + File.separator + "unitTestTempDir");
        if (tempDir.exists()) {
            com.transifex.common.Utils.deleteDirectory(tempDir);
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

    public static LocaleData.LocaleStrings getElLocaleStrings() {
        HashMap<String, LocaleData.StringInfo> map = new HashMap<>();
        map.put("test_key", new LocaleData.StringInfo("Καλημέρα"));
        map.put("test_key3", new LocaleData.StringInfo(""));
        return new LocaleData.LocaleStrings(map);
    }

    public static LocaleData.LocaleStrings getEsLocaleStrings() {
        HashMap<String, LocaleData.StringInfo> map = new HashMap<>();
        map.put("test_key", new LocaleData.StringInfo("Buenos días"));
        map.put("test_key3", new LocaleData.StringInfo(""));
        return new LocaleData.LocaleStrings(map);
    }

    public static LocaleData.TranslationMap getElEsTranslationMap() {
        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("el", getElLocaleStrings());
        translationMap.put("es", getEsLocaleStrings());
        return translationMap;
    }

    @Test
    // Check that we can read what was written
    public void testToDiskFromDisk_normal() {
        boolean tempDirCreated =  tempDir.mkdirs();
        assertThat(tempDirCreated).isTrue();

        LocaleData.TranslationMap translationMap = getElEsTranslationMap();
        TranslationMapStorage storage = new TranslationMapStorage("strings.txt");
        HashMap<String, File> files = storage.toDisk(translationMap, tempDir);

        assertThat(files).isNotNull();
        assertThat(files.keySet()).containsExactly("el", "es");

        LocaleData.TranslationMap map = storage.fromDisk(tempDir);
        assertThat(map).isNotNull();
        assertThat(map).isEqualTo(translationMap);
    }
}
