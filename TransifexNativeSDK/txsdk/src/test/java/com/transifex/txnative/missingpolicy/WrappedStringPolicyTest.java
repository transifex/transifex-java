package com.transifex.txnative.missingpolicy;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class WrappedStringPolicyTest {

    @Test
    public void testGet_normal() {
        WrappedStringPolicy policy = new WrappedStringPolicy("<", " !end");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.get(sourceString);

        assertThat(translated).isEqualTo("<The quick\n brown fox !end");
    }

    @Test
    public void testGet_startIsNull_justEndIsAppended() {
        WrappedStringPolicy policy = new WrappedStringPolicy(null, " !end");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.get(sourceString);

        assertThat(translated).isEqualTo("The quick\n brown fox !end");
    }

    @Test
    public void testGet_startIsEmpty_justEndIsAppended() {
        WrappedStringPolicy policy = new WrappedStringPolicy("", " !end");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.get(sourceString);

        assertThat(translated).isEqualTo("The quick\n brown fox !end");
    }

    @Test
    public void testGet_endIsNull_justStartIsPrefixed() {
        WrappedStringPolicy policy = new WrappedStringPolicy("<", null);
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.get(sourceString);

        assertThat(translated).isEqualTo("<The quick\n brown fox");
    }

    @Test
    public void testGet_endIsEmpty_justStartIsPrefixed() {
        WrappedStringPolicy policy = new WrappedStringPolicy("<", "");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.get(sourceString);

        assertThat(translated).isEqualTo("<The quick\n brown fox");
    }
}