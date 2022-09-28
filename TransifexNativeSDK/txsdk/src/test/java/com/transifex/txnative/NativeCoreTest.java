package com.transifex.txnative;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.transifex.common.LocaleData;
import com.transifex.txnative.cache.TxMemoryCache;
import com.transifex.txnative.test.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import androidx.test.core.app.ApplicationProvider;

import static android.text.Html.FROM_HTML_MODE_LEGACY;
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

    private TxMemoryCache getEnElMemoryCache() {
        HashMap<String, LocaleData.StringInfo> dic1 = new HashMap<>();
        dic1.put("tx_test_key", new LocaleData.StringInfo("test updated"));
        dic1.put("tx_plural_test_key", new LocaleData.StringInfo("{cnt, plural, one {car updated} other {cars updated} two {car 2 updated}}"));
        LocaleData.LocaleStrings enStrings = new LocaleData.LocaleStrings(dic1);

        HashMap<String, LocaleData.StringInfo> dic2 = new HashMap<>();
        dic2.put("tx_test_key", new LocaleData.StringInfo("test ελ tx"));
        dic2.put("tx_plural_test_key", new LocaleData.StringInfo("{cnt, plural, one {αυτοκίνητο tx} other {αυτοκίνητα tx}}"));
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(dic2);

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("en", enStrings);
        translationMap.put("el", elStrings);

        TxMemoryCache memoryCache = new TxMemoryCache();
        memoryCache.update(translationMap);

        return memoryCache;
    }

    private TxMemoryCache getSLElMemoryCache() {
        HashMap<String, LocaleData.StringInfo> dic1 = new HashMap<>();
        dic1.put("tx_test_key", new LocaleData.StringInfo("test updated"));
        dic1.put("tx_plural_test_key", new LocaleData.StringInfo("{cnt, plural, one {car updated} other {cars updated} two {car 2 updated}}"));
        LocaleData.LocaleStrings slStrings = new LocaleData.LocaleStrings(dic1);

        HashMap<String, LocaleData.StringInfo> dic2 = new HashMap<>();
        dic2.put("tx_test_key", new LocaleData.StringInfo("test ελ tx"));
        dic2.put("tx_plural_test_key", new LocaleData.StringInfo("{cnt, plural, one {αυτοκίνητο tx} other {αυτοκίνητα tx}}"));
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(dic2);

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("sl", slStrings);
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
    private static final String STRING_WITHOUT_TAGS_AND_HTML_ENTITIES = "there is a new\nline &amp; multiple   spaces";
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
    @Config(qualifiers = "en")
    public void testTranslate_androidUsesSourceLocale_sourceStringsProvided_returnSourceStringFromCache() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache memoryCache = getEnElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, memoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        // We expect to get the source string from the cache, not from strings.xml
        assertThat(string).isEqualTo("test updated");
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
    @Config(qualifiers = "es-rES")
    public void testTranslateWithDefaultMissingPolicy_androidUsesUnsupportedLocale_sourceStringsProvided_returnSourceStringFromCache() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache memoryCache = getEnElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, memoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string = nativeCore.translate(txResources, R.string.tx_test_key);

        assertThat(string).isEqualTo("test updated");
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
    public void testGetSpannedString_spanSupportEnabledWithHTMLEscapedString_returnStringWithTags() {
        // We expect to get a String object, since no tags exist in the  parsed string.
        // However, "&lt;" should be converted to "<", so that tags exist in the returned string.

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache dummyCache = getEmptyMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, dummyCache, null);
        nativeCore.setSupportSpannable(true);

        CharSequence string = nativeCore.getSpannedString(STRING_WITH_TAGS_HTML_ESCAPED);

        assertThat(string).isInstanceOf(String.class);

        string = Utils.fromHtml(string.toString(), FROM_HTML_MODE_LEGACY);

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
    public void testGetSpannedString_spanSupportEnabledWithSimpleStringWithHTMLEntities_returnStringWithUnescapedHTMLEntities() {
        // We expect to get a String object, since no tags exist in the  parsed string. New lines and
        // multiple spaces should be preserved. "&amp;" should be converted to "&"

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache dummyCache = getEmptyMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, dummyCache, null);
        nativeCore.setSupportSpannable(true);

        CharSequence string = nativeCore.getSpannedString(STRING_WITHOUT_TAGS_AND_HTML_ENTITIES);

        assertThat(string).isInstanceOf(String.class);
        assertThat(string).isEqualTo("there is a new\nline & multiple   spaces");
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

        string = Utils.fromHtml(string.toString(), FROM_HTML_MODE_LEGACY);

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

        string = Utils.fromHtml(string.toString(), FROM_HTML_MODE_LEGACY);

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
    public void testGetSpannedString_spanSupportDisabledWithSimpleStringWithHTMLEntities_returnStringWithUnescapedHTMLEntities() {
        // We expect to get a String object, since no tags exist in the  parsed string. New lines and
        // multiple spaces should be preserved. "&amp;" should be converted to "&"

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache dummyCache = getEmptyMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, dummyCache, null);
        nativeCore.setSupportSpannable(false);

        CharSequence string = nativeCore.getSpannedString(STRING_WITHOUT_TAGS_AND_HTML_ENTITIES);

        assertThat(string).isInstanceOf(String.class);
        assertThat(string).isEqualTo("there is a new\nline & multiple   spaces");
    }

    // endregion spanned string

    // region get localized quantity string

    @Test
    @Config(qualifiers = "en")
    public void testGetLocalizedQuantityString_androidUsesEN() {
        // https://unicode-org.github.io/cldr-staging/charts/37/supplemental/language_plural_rules.html#en

        Resources resources = mockContext.getResources();

        String quantityStringForZero = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 0);
        String quantityStringForOne = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 1);
        String quantityStringForTwo = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 2);
        String quantityStringForThree = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 3);
        String quantityStringForALot = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 20);

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

        Resources resources = mockContext.getResources();

        String quantityStringForZero = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 0);
        String quantityStringForOne = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 1);
        String quantityStringForTwo = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 2);
        String quantityStringForThree = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 3);
        String quantityStringForALot = NativeCore.getLocalizedQuantityString(resources, ICU_STRING, 20);

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

        Resources resources = mockContext.getResources();

        String quantityStringForZero = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_SIMPLE, 0);
        String quantityStringForOne = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_SIMPLE, 1);
        String quantityStringForTwo = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_SIMPLE, 2);
        String quantityStringForThree = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_SIMPLE, 3);
        String quantityStringForALot = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_SIMPLE, 20);

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

        Resources resources = mockContext.getResources();

        String quantityStringForZero = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_OTHER_NOT_SPECIFIED, 0);
        String quantityStringForOne = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_OTHER_NOT_SPECIFIED, 1);
        String quantityStringForTwo = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_OTHER_NOT_SPECIFIED, 2);
        String quantityStringForThree = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_OTHER_NOT_SPECIFIED, 3);
        String quantityStringForALot = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_OTHER_NOT_SPECIFIED, 20);

        assertThat(quantityStringForZero).isNull();
        assertThat(quantityStringForOne).isNull();
        assertThat(quantityStringForTwo).isNull();
        assertThat(quantityStringForThree).isNull();
        assertThat(quantityStringForALot).isNull();
    }

    @Test
    @Config(qualifiers = "en")
    public void testGetLocalizedQuantityString_ICUStringWithOtherNotSpecified_returnNull() {
        Resources resources = mockContext.getResources();

        String quantityStringForZero = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_EMPTY, 0);
        String quantityStringForOne = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_EMPTY, 1);
        String quantityStringForTwo = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_EMPTY, 2);
        String quantityStringForThree = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_EMPTY, 3);
        String quantityStringForALot = NativeCore.getLocalizedQuantityString(resources, ICU_STRING_EMPTY, 20);

        assertThat(quantityStringForZero).isNull();
        assertThat(quantityStringForOne).isNull();
        assertThat(quantityStringForTwo).isNull();
        assertThat(quantityStringForThree).isNull();
        assertThat(quantityStringForALot).isNull();
    }

    @Test
    @Config(qualifiers = "en")
    public void testGetLocalizedQuantityString_ICUStringIsEmpty_returnNull() {
        Resources resources = mockContext.getResources();

        String quantityStringForZero = NativeCore.getLocalizedQuantityString(resources, "", 0);
        String quantityStringForOne = NativeCore.getLocalizedQuantityString(resources, "", 1);
        String quantityStringForTwo = NativeCore.getLocalizedQuantityString(resources, "", 2);
        String quantityStringForThree = NativeCore.getLocalizedQuantityString(resources, "", 3);
        String quantityStringForALot = NativeCore.getLocalizedQuantityString(resources, "", 20);

        assertThat(quantityStringForZero).isNull();
        assertThat(quantityStringForOne).isNull();
        assertThat(quantityStringForTwo).isNull();
        assertThat(quantityStringForThree).isNull();
        assertThat(quantityStringForALot).isNull();
    }

    // endregion get localized quantity string

    // region translate quantity string

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
    @Config(qualifiers = "en")
    public void testTranslateQuantityString_androidUsesSourceLocale_sourceStringsProvided_returnSourceStringFromCache() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache memoryCache = getEnElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, memoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("car updated");
        assertThat(stringTwo).isEqualTo("cars updated");
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
    @Config(qualifiers = "es-rES")
    public void testTranslateQuantityStringWithDefaultMissingPolicy_ENSourceLocaleAndAndroidUsesUnsupportedLocale_sourceStringsProvided_returnSourceStringFromCache() {
        // The quantity string will be rendered using the source locale plural rules, which in this
        // case is "en" and the source strings provided by the cache

        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache memoryCache = getEnElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, memoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("car updated");
        assertThat(stringTwo).isEqualTo("cars updated");
    }

    @Test
    @Config(qualifiers = "es-rES")
    public void testTranslateQuantityStringWithDefaultMissingPolicy_SLSourceLocaleAndAndroidUsesUnsupportedLocale_sourceStringsProvided_returnSourceStringFromCache() {
        // The quantity string will be rendered using the source locale plural rules, which in this
        // case is "sl" and the source strings provided by the cache

        LocaleState localeState = new LocaleState(mockContext,
                "sl",
                new String[]{"sl", "el"},
                null);
        TxMemoryCache memoryCache = getSLElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, memoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = nativeCore.translateQuantityString(txResources, R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("car updated");
        assertThat(stringTwo).isEqualTo("car 2 updated");
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

    // endregion translate quantity string
}