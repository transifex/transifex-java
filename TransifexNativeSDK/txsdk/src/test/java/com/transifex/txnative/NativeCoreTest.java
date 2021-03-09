package com.transifex.txnative;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.transifex.common.LocaleData;
import com.transifex.txnative.cache.TxMemoryCache;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import androidx.core.text.HtmlCompat;
import androidx.test.core.app.ApplicationProvider;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class NativeCoreTest {

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

    private TxMemoryCache getElMemoryCache() {
        HashMap<String, LocaleData.StringInfo> dic2 = new HashMap<>();
        dic2.put("tx_test_key", new LocaleData.StringInfo("test ελ tx"));
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(dic2);

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("el", elStrings);

        TxMemoryCache memoryCache = new TxMemoryCache();
        memoryCache.update(translationMap);

        return memoryCache;
    }

    private TxMemoryCache getElSpanMemoryCache() {
        HashMap<String, LocaleData.StringInfo> dic2 = new HashMap<>();
        dic2.put("tx_test_key", new LocaleData.StringInfo("this is <b>bold</b>"));
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(dic2);

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("el", elStrings);

        TxMemoryCache memoryCache = new TxMemoryCache();
        memoryCache.update(translationMap);

        return memoryCache;
    }

    private TxMemoryCache getElHTMLEscapedMemoryCache() {
        HashMap<String, LocaleData.StringInfo> dic2 = new HashMap<>();
        dic2.put("tx_test_key", new LocaleData.StringInfo("this is &lt;b>bold&lt;/b>"));
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(dic2);

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("el", elStrings);

        TxMemoryCache memoryCache = new TxMemoryCache();
        memoryCache.update(translationMap);

        return memoryCache;
    }

    private TxMemoryCache getEmptyMemoryCache() {
        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);

        TxMemoryCache memoryCache = new TxMemoryCache();
        memoryCache.update(translationMap);

        return memoryCache;
    }

    @Test
    @Config(qualifiers = "en")
    public void testTranslate_sourceLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isEqualTo("test");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslate_supportedLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isEqualTo("test ελ tx");
    }

    @Test
    @Config(qualifiers = "es-rES")
    public void testTranslate_defaultMissingPolicy_unsupportedLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isEqualTo("test");
    }

    @Test
    public void testTranslate_idDoesNotExist() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        final NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        final TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        assertThrows(Resources.NotFoundException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                CharSequence string = nativeCore.translate(txResources, 0);
            }
        });
    }

    @Test
    public void testTranslateDefault_idDoesNotExist() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        final NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        final TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, 0, "default");

        assertThat(string).isEqualTo("default");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslate_testMode() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        final NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        nativeCore.setTestMode(true);
        final TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isEqualTo("test: test ελ");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslate_testMode_androidResource() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        nativeCore.setTestMode(true);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence androidString = mockContext.getResources().getString(android.R.string.ok);
        CharSequence string = nativeCore.translate(txResources, android.R.string.ok);

        assertThat(string).isEqualTo(androidString);
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslate_spanSupportEnabled_spanString() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElSpanMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        nativeCore.setSupportSpannable(true);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isInstanceOf(Spanned.class);

        CharSequence styledPart = null;

        if (string instanceof Spanned) {
            Spanned spanned = (Spanned) string;
            StyleSpan[] spans = spanned.getSpans(0, spanned.length(), StyleSpan.class);
            StyleSpan span = spans[0];
            int start = spanned.getSpanStart(span);
            int end = spanned.getSpanEnd(span);
            styledPart = spanned.subSequence(start, end);
        }

        assertThat(styledPart).isNotNull();
        assertThat(styledPart.toString()).isEqualTo("bold");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslate_spanSupportEnabled_HTMLEscapedString() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElHTMLEscapedMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        nativeCore.setSupportSpannable(true);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isInstanceOf(String.class);

        string = HtmlCompat.fromHtml(string.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);

        CharSequence styledPart = null;

        {
            Spanned spanned = (Spanned) string;
            StyleSpan[] spans = spanned.getSpans(0, spanned.length(), StyleSpan.class);
            StyleSpan span = spans[0];
            int start = spanned.getSpanStart(span);
            int end = spanned.getSpanEnd(span);
            styledPart = spanned.subSequence(start, end);
        }

        assertThat(styledPart).isNotNull();
        assertThat(styledPart.toString()).isEqualTo("bold");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslate_spanSupportEnabled_simpleString() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        nativeCore.setSupportSpannable(true);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isInstanceOf(String.class);
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslate_spanSupportDisabled_spanString() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElSpanMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isInstanceOf(String.class);

        string = HtmlCompat.fromHtml(string.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);

        CharSequence styledPart = null;

        {
            Spanned spanned = (Spanned) string;
            StyleSpan[] spans = spanned.getSpans(0, spanned.length(), StyleSpan.class);
            StyleSpan span = spans[0];
            int start = spanned.getSpanStart(span);
            int end = spanned.getSpanEnd(span);
            styledPart = spanned.subSequence(start, end);
        }

        assertThat(styledPart).isNotNull();
        assertThat(styledPart.toString()).isEqualTo("bold");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslate_spanSupportDisabled_HTMLEscapedString() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElHTMLEscapedMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isInstanceOf(String.class);

        string = HtmlCompat.fromHtml(string.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);

        CharSequence styledPart = null;

        {
            Spanned spanned = (Spanned) string;
            StyleSpan[] spans = spanned.getSpans(0, spanned.length(), StyleSpan.class);
            StyleSpan span = spans[0];
            int start = spanned.getSpanStart(span);
            int end = spanned.getSpanEnd(span);
            styledPart = spanned.subSequence(start, end);
        }

        assertThat(styledPart).isNotNull();
        assertThat(styledPart.toString()).isEqualTo("bold");
    }


}