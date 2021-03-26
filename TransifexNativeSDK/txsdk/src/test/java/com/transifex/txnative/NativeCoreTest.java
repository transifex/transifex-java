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
        dic2.put("tx_plural_test_key", new LocaleData.StringInfo("{cnt, plural, one {αυτοκίνητο tx} other {αυτοκίνητα tx}}"));
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

    private static final String STRING_WITHOUT_TAGS = "this is not bold";
    private static final String STRING_WITH_TAGS = "this is <b>bold</b>";
    private static final String STRING_WITH_TAGS_HTML_ESCAPED = "this is &lt;b>bold&lt;/b>";

    private static final String ICU_STRING = "{cnt, plural, zero {zero} one {this is one} two {just two} few {just a few} many {a lot!} other {others!}}";
    private static final String ICU_STRING_SIMPLE = "{cnt, plural, one {this is one} other {others!}}";
    private static final String ICU_STRING_OTHER_NOT_SPECIFIED = "{cnt, plural, one {this is one}}";
    private static final String ICU_STRING_EMPTY = "{cnt, plural, }";

    // region translate

    @Test
    @Config(qualifiers = "en")
    public void testTranslate_androidUsesSourceLocale() {
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
    public void testTranslate_androidUsesSupportedLocale() {
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
    public void testTranslateWithDefaultMissingPolicy_androidUsesUnsupportedLocale_returnSourceString() {
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
    public void testTranslate_idDoesNotExist_exceptionIsThrown() {
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
    public void testTranslateWithDefaultString_idDoesNotExist_defaultStringIsReturned() {
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
    public void testTranslate_testMode_returnPrefixedString() {
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
    public void testTranslate_testModeAndUseAndroidResourceString_returnedStringNotPrefixed() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        nativeCore.setTestMode(true);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence androidString = mockContext.getResources().getString(android.R.string.cancel);
        CharSequence string = nativeCore.translate(txResources, android.R.string.cancel);

        assertThat(string).isEqualTo(androidString);
    }

    // endregion translate

    // region spanned string

    @Test
    @Config(qualifiers = "en")
    public void testGetSpannedString_spanSupportEnabled_returnSpannedString() {
        // We expect the tags to be parsed into spans

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache dummyCache = getEmptyMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, dummyCache, null);
        nativeCore.setSupportSpannable(true);

        CharSequence string = nativeCore.getSpannedString(STRING_WITH_TAGS);

        assertThat(string).isInstanceOf(Spanned.class);

        CharSequence styledPart = null;

        Spanned spanned = (Spanned) string;
        StyleSpan[] spans = spanned.getSpans(0, spanned.length(), StyleSpan.class);
        StyleSpan span = spans[0];
        int start = spanned.getSpanStart(span);
        int end = spanned.getSpanEnd(span);
        styledPart = spanned.subSequence(start, end);


        assertThat(styledPart).isNotNull();
        assertThat(styledPart.toString()).isEqualTo("bold");
    }

    @Test
    @Config(qualifiers = "en")
    public void testGetSpannedString_spanSupportEnabledWithHTMLEscapedString_returnSpannedString() {
        // We expect the tags, even though they use "&lt;" instead of "<" to be parsed into spans

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache dummyCache = getEmptyMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, dummyCache, null);
        nativeCore.setSupportSpannable(true);

        CharSequence string = nativeCore.getSpannedString(STRING_WITH_TAGS_HTML_ESCAPED);

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
    @Config(qualifiers = "en")
    public void testGetSpannedString_spanSupportEnabledWithSimpleString_returnString() {
        // We expect to get a String object, since no tags exist in the  parsed string

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache dummyCache = getEmptyMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, dummyCache, null);
        nativeCore.setSupportSpannable(true);

        CharSequence string = nativeCore.getSpannedString(STRING_WITHOUT_TAGS);

        assertThat(string).isInstanceOf(String.class);
    }

    @Test
    @Config(qualifiers = "en")
    public void testGetSpannedString_spanSupportDisabled_returnStringWithTags() {
        // We expect to get a String object, because span support is disabled. The tags of the
        // parsed string should be kept as-is.

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache dummyCache = getEmptyMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, dummyCache, null);

        CharSequence string = nativeCore.getSpannedString(STRING_WITH_TAGS);

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
    @Config(qualifiers = "en")
    public void testGetSpannedString_spanSupportDisabledWithHTMLEscapedString_returnStringWithTags() {
        // We expect to get a String object, because span support is disabled. "&lt;" should be
        // converted to "<", so that tags exist in the returned string.

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache dummyCache = getEmptyMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, dummyCache, null);

        CharSequence string = nativeCore.getSpannedString(STRING_WITH_TAGS_HTML_ESCAPED);

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

    // endregion spanned string

    // region translate quantity string

    @Test
    @Config(qualifiers = "en")
    public void testGetLocalizedQuantityString_androidUsesEN() {
        // https://unicode-org.github.io/cldr-staging/charts/37/supplemental/language_plural_rules.html#en

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        String quantityStringForZero = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 0);
        String quantityStringForOne = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 1);
        String quantityStringForTwo = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 2);
        String quantityStringForThree = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 3);
        String quantityStringForALot = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 20);

        assertThat(quantityStringForZero).isEqualTo("others!");
        assertThat(quantityStringForOne).isEqualTo("this is one");
        assertThat(quantityStringForTwo).isEqualTo("others!");
        assertThat(quantityStringForThree).isEqualTo("others!");
        assertThat(quantityStringForALot).isEqualTo("others!");
    }

    @Test
    @Config(qualifiers = "sl")
    public void testGetLocalizedQuantityString_androidUsesSL() {
        // https://unicode-org.github.io/cldr-staging/charts/37/supplemental/language_plural_rules.html#sl

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        String quantityStringForZero = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 0);
        String quantityStringForOne = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 1);
        String quantityStringForTwo = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 2);
        String quantityStringForThree = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 3);
        String quantityStringForALot = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING, 20);

        assertThat(quantityStringForZero).isEqualTo("others!");
        assertThat(quantityStringForOne).isEqualTo("this is one");
        assertThat(quantityStringForTwo).isEqualTo("just two");
        assertThat(quantityStringForThree).isEqualTo("just a few");
        assertThat(quantityStringForALot).isEqualTo("others!");
    }

    @Test
    @Config(qualifiers = "sl")
    public void testGetLocalizedQuantityString_androidUsesSLAndICUStringWithoutAllPlurals_fallbackToOthersPlural() {
        // https://unicode-org.github.io/cldr-staging/charts/37/supplemental/language_plural_rules.html#sl

        // In this test, the ICU does not have "TWO" and "FEW" plurals. We expect the "OTHERS" plural
        // to be used as fallback

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        String quantityStringForZero = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_SIMPLE, 0);
        String quantityStringForOne = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_SIMPLE, 1);
        String quantityStringForTwo = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_SIMPLE, 2);
        String quantityStringForThree = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_SIMPLE, 3);
        String quantityStringForALot = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_SIMPLE, 20);

        assertThat(quantityStringForZero).isEqualTo("others!");
        assertThat(quantityStringForOne).isEqualTo("this is one");
        assertThat(quantityStringForTwo).isEqualTo("others!"); // fallback
        assertThat(quantityStringForThree).isEqualTo("others!"); //fallback
        assertThat(quantityStringForALot).isEqualTo("others!");
    }

    @Test
    @Config(qualifiers = "sl")
    public void testGetLocalizedQuantityString_androidUsesSLAndICUStringWithoutOthersPlural_returnNull() {
        // https://unicode-org.github.io/cldr-staging/charts/37/supplemental/language_plural_rules.html#sl

        // In this test, the ICU only has the "ONE" plural. Since the "OTHERS" plural does not exist,
        // we expect "null" to be returned for any given quantity.

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        String quantityStringForZero = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_OTHER_NOT_SPECIFIED, 0);
        String quantityStringForOne = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_OTHER_NOT_SPECIFIED, 1);
        String quantityStringForTwo = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_OTHER_NOT_SPECIFIED, 2);
        String quantityStringForThree = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_OTHER_NOT_SPECIFIED, 3);
        String quantityStringForALot = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_OTHER_NOT_SPECIFIED, 20);

        assertThat(quantityStringForZero).isNull();
        assertThat(quantityStringForOne).isNull();
        assertThat(quantityStringForTwo).isNull();
        assertThat(quantityStringForThree).isNull();
        assertThat(quantityStringForALot).isNull();
    }

    @Test
    @Config(qualifiers = "en")
    public void testGetLocalizedQuantityString_ICUStringWithOtherNotSpecified_returnNull() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        String quantityStringForZero = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_EMPTY, 0);
        String quantityStringForOne = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_EMPTY, 1);
        String quantityStringForTwo = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_EMPTY, 2);
        String quantityStringForThree = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_EMPTY, 3);
        String quantityStringForALot = nativeCore.getLocalizedQuantityString(txResources, ICU_STRING_EMPTY, 20);

        assertThat(quantityStringForZero).isNull();
        assertThat(quantityStringForOne).isNull();
        assertThat(quantityStringForTwo).isNull();
        assertThat(quantityStringForThree).isNull();
        assertThat(quantityStringForALot).isNull();
    }

    @Test
    @Config(qualifiers = "en")
    public void testGetLocalizedQuantityString_ICUStringIsEmpty_returnNull() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        String quantityStringForZero = nativeCore.getLocalizedQuantityString(txResources, "", 0);
        String quantityStringForOne = nativeCore.getLocalizedQuantityString(txResources, "", 1);
        String quantityStringForTwo = nativeCore.getLocalizedQuantityString(txResources, "", 2);
        String quantityStringForThree = nativeCore.getLocalizedQuantityString(txResources, "", 3);
        String quantityStringForALot = nativeCore.getLocalizedQuantityString(txResources, "", 20);

        assertThat(quantityStringForZero).isNull();
        assertThat(quantityStringForOne).isNull();
        assertThat(quantityStringForTwo).isNull();
        assertThat(quantityStringForThree).isNull();
        assertThat(quantityStringForALot).isNull();
    }

    @Test
    @Config(qualifiers = "en")
    public void testTranslateQuantityString_androidUsesSourceLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("car");
        assertThat(stringTwo).isEqualTo("cars");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslateQuantityString_androidUsesSupportedLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("αυτοκίνητο tx");
        assertThat(stringTwo).isEqualTo("αυτοκίνητα tx");
    }

    @Test
    @Config(qualifiers = "es-rES")
    public void testTranslateQuantityStringWithDefaultMissingPolicy_ENSourceLocaleAndAndroidUsesUnsupportedLocale_returnSourceString() {
        // The quantity string will be rendered using the source locale plural rules, which in this
        // case is "en" and the default source strings

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("car");
        assertThat(stringTwo).isEqualTo("cars");
    }

    @Test
    @Config(qualifiers = "es-rES")
    public void testTranslateQuantityStringWithDefaultMissingPolicy_SLSourceLocaleAndAndroidUsesUnsupportedLocale_returnSourceString() {
        // The quantity string will be rendered using the source locale plural rules, which in this
        // case is "sl" and the default source strings

        LocaleState localeState = new LocaleState(mockContext,
                "sl",
                new String[]{"sl", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("car");
        assertThat(stringTwo).isEqualTo("car 2");
    }

    @Test
    public void testTranslateQuantityString_idDoesNotExist_exceptionIsThrown() {
        LocaleState localeState = new LocaleState(mockContext,
                "sl",
                new String[]{"sl", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        final NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        final TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        assertThrows(Resources.NotFoundException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                CharSequence stringOne = nativeCore.translateQuantityString(txResources, 0, 1);
            }
        });
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testTranslateQuantityString_testMode_returnPrefixedString() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        nativeCore.setTestMode(true);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("test: αυτοκίνητο");
        assertThat(stringTwo).isEqualTo("test: αυτοκίνητα");
    }

    // endregion quantity string
}