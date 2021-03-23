package com.transifex.txnative;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.transifex.common.LocaleData;
import com.transifex.txnative.cache.TxMemoryCache;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import androidx.test.core.app.ApplicationProvider;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class TxResourcesTest {

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
        dic2.put("tx_test_key", new LocaleData.StringInfo("test ελ %d tx"));
        dic2.put("tx_plural_test_key", new LocaleData.StringInfo("{cnt, plural, one {%d αυτοκίνητο} other {%d αυτοκίνητα}}"));
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(dic2);

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("el", elStrings);

        TxMemoryCache memoryCache = new TxMemoryCache();
        memoryCache.update(translationMap);

        return memoryCache;
    }

    // region interface

    @Test
    public void testIsAndroidStringResource() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        assertThat(txResources.isAndroidStringResource(android.R.string.cancel)).isTrue();
        assertThat(txResources.isAndroidStringResource(R.string.tx_test_key)).isFalse();
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetOriginalText() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string =  txResources.getOriginalText(R.string.tx_test_key);

        assertThat(string).isEqualTo("test ελ");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetOriginalQuantityText() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string =  txResources.getOriginalQuantityText(R.plurals.tx_plural_test_key, 2);

        assertThat(string).isEqualTo("αυτοκίνητα");
    }


    // endregion interface

    // region overrides

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetText_androidUsesSupportedLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string =  txResources.getText(R.string.tx_test_key);

        assertThat(string).isEqualTo("test ελ %d tx");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetText_androidUsesSupportedLocaleAndIdDoesNotExist_exceptionIsThrown() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        final TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        assertThrows(Resources.NotFoundException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                CharSequence string =  txResources.getText(0);
            }
        });
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetTextDef_androidUsesSupportedLocaleAndIdDoesNotExist_defaultStringIsReturned() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string =  txResources.getText(0, "default string");

        assertThat(string).isEqualTo("default string");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetString_androidUsesSupportedLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string =  txResources.getString(R.string.tx_test_key);

        assertThat(string).isEqualTo("test ελ %d tx");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetStringFormat_androidUsesSupportedLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence string =  txResources.getString(R.string.tx_test_key, 9);

        assertThat(string).isEqualTo("test ελ 9 tx");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetQuantityText_androidUsesSupportedLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = txResources.getQuantityText(R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = txResources.getQuantityText(R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("%d αυτοκίνητο");
        assertThat(stringTwo).isEqualTo("%d αυτοκίνητα");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetQuantityText_androidUsesSupportedLocaleAndIdDoesNotExist_exceptionIsThrown() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        final TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        assertThrows(Resources.NotFoundException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                CharSequence stringOne = txResources.getQuantityText(0, 1);
            }
        });
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetQuantityString_androidUsesSupportedLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = txResources.getQuantityString(R.plurals.tx_plural_test_key, 1);
        CharSequence stringTwo = txResources.getQuantityString(R.plurals.tx_plural_test_key, 2);

        assertThat(stringOne).isEqualTo("%d αυτοκίνητο");
        assertThat(stringTwo).isEqualTo("%d αυτοκίνητα");
    }

    @Test
    @Config(qualifiers = "el-rGR")
    public void testGetQuantityStringFormat_androidUsesSupportedLocale() {
        LocaleState localeState = new LocaleState(mockContext,
                "en",
                new String[]{"en", "el"},
                null);
        TxMemoryCache elMemoryCache = getElMemoryCache();
        NativeCore nativeCore = new NativeCore(mockContext, localeState, "token", null, elMemoryCache, null);
        TxResources txResources = new TxResources(mockContext.getResources(), nativeCore);

        CharSequence stringOne = txResources.getQuantityString(R.plurals.tx_plural_test_key, 1, 1);
        CharSequence stringTwo = txResources.getQuantityString(R.plurals.tx_plural_test_key, 2, 2);

        assertThat(stringOne).isEqualTo("1 αυτοκίνητο");
        assertThat(stringTwo).isEqualTo("2 αυτοκίνητα");
    }

    // endregion overrides
}