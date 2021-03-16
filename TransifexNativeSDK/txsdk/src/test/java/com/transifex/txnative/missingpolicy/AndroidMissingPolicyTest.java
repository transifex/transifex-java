package com.transifex.txnative.missingpolicy;

import android.content.Context;
import android.os.Build;

import com.transifex.txnative.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class AndroidMissingPolicyTest {

    // The tests rely on the following directories:
    //
    // test/res/values
    // test/res/values-el
    // test/res/values-es

    private Context mockContext;

    @Before
    public void setUp() {
        // inject context provided by Robolectric
        mockContext = ApplicationProvider.getApplicationContext();
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGet() {
        // We test if AndroidMissingPolicy can return the el translation found in the app's
        // test/res/values-el/strings.xml

        CharSequence sourceString = "dummy source string";
        String resourceEntryName = mockContext.getResources().getResourceEntryName(R.string.tx_test_key);

        AndroidMissingPolicy policy = new AndroidMissingPolicy(mockContext);
        CharSequence translated = policy.get(sourceString, R.string.tx_test_key, resourceEntryName, "el");

        assertThat(translated).isEqualTo("test ελ");
    }
}