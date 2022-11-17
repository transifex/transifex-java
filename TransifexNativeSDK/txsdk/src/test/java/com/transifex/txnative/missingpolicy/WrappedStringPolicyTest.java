package com.transifex.txnative.missingpolicy;

import android.content.res.Resources;
import android.os.Build;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.StyleSpan;

import com.transifex.txnative.Utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class WrappedStringPolicyTest {

    final int stringId = 0;
    final String stringResourceName = "dummy_name";
    final String locale = "el";

    private Resources resources;

    @Before
    public void setUp() {
        resources = mock(Resources.class);
    }

    @Test
    public void testWrapString_normal() {
        WrappedStringPolicy policy = new WrappedStringPolicy("<", " !end");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.wrapString(sourceString);

        assertThat(translated).isEqualTo("<The quick\n brown fox !end");
    }

    @Test
    public void testWrapString_startIsNull_justEndIsAppended() {
        WrappedStringPolicy policy = new WrappedStringPolicy(null, " !end");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.wrapString(sourceString);

        assertThat(translated).isEqualTo("The quick\n brown fox !end");
    }

    @Test
    public void testWrapString_startIsEmpty_justEndIsAppended() {
        WrappedStringPolicy policy = new WrappedStringPolicy("", " !end");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.wrapString(sourceString);

        assertThat(translated).isEqualTo("The quick\n brown fox !end");
    }

    @Test
    public void testWrapString_endIsNull_justStartIsPrefixed() {
        WrappedStringPolicy policy = new WrappedStringPolicy("<", null);
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.wrapString(sourceString);

        assertThat(translated).isEqualTo("<The quick\n brown fox");
    }

    @Test
    public void testWrapString_endIsEmpty_justStartIsPrefixed() {
        WrappedStringPolicy policy = new WrappedStringPolicy("<", "");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.wrapString(sourceString);

        assertThat(translated).isEqualTo("<The quick\n brown fox");
    }

    @Test
    public void testWrapString_spannedString() {
        // Test that a spanned source string keeps its spans after being processed

        WrappedStringPolicy policy = new WrappedStringPolicy("start! ", " !end");
        CharSequence sourceString = Utils.fromHtml("The quick <b>brown</b> fox", FROM_HTML_MODE_LEGACY);
        CharSequence translated = policy.wrapString(sourceString);

        assertThat(translated).isInstanceOf(Spanned.class);
        Spanned spanned = (Spanned) translated;
        StyleSpan[] spans = spanned.getSpans(0, spanned.length(), StyleSpan.class);
        StyleSpan span = spans[0];
        int start = spanned.getSpanStart(span);
        int end = spanned.getSpanEnd(span);
        CharSequence styledPart = spanned.subSequence(start, end);

        assertThat(styledPart).isNotNull();
        assertThat(styledPart.toString()).isEqualTo("brown");

        assertThat(translated.toString()).isEqualTo("start! The quick brown fox !end");
    }

    @Test
    public void testGet_normal() {
        WrappedStringPolicy policy = new WrappedStringPolicy("<", " !end");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.get(resources, sourceString, stringId, stringResourceName, locale);

        assertThat(translated).isEqualTo("<The quick\n brown fox !end");
    }

    @Test
    public void testGetQuantityString_normal() {
        WrappedStringPolicy policy = new WrappedStringPolicy("<", " !end");
        String sourceString = "The quick\n brown fox";
        CharSequence translated = policy.getQuantityString(resources, sourceString, stringId, 1, stringResourceName, locale);

        assertThat(translated).isEqualTo("<The quick\n brown fox !end");
    }
}