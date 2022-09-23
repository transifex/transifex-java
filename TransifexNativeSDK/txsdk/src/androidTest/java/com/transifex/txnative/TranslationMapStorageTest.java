package com.transifex.txnative;

import android.content.Context;

import com.transifex.common.LocaleData;
import com.transifex.common.StringTestData;
import com.transifex.common.TempDirHelper;
import com.transifex.common.TranslationMapStorage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TranslationMapStorageTest {

    // The following test is copied from the respective unit test.

    Context appContext = null;
    TempDirHelper tempDirHelper = null;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File filesDir = appContext.getFilesDir();
        File tempDir = new File(filesDir.getPath() + File.separator + "unitTestTempDir");
        tempDirHelper = new TempDirHelper(tempDir);
        tempDirHelper.setUp();
    }

    @After
    public void Teardown() {
        if (tempDirHelper != null) {
            tempDirHelper.tearDown();
        }
    }

    @Test
    // Check that we can read what was written
    public void testToDiskFromDisk_normal() {
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
