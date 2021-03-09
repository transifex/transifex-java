package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TxProviderBasedCacheTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private LocaleData.TranslationMap getElTranslationMap1() {
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(1);
        elStrings.put("tx_test_key", new LocaleData.StringInfo("test ελ tx"));

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(1);
        translationMap.put("el", elStrings);

        return translationMap;
    }

    private LocaleData.TranslationMap getElTranslationMap2() {
        LocaleData.LocaleStrings elStrings = new LocaleData.LocaleStrings(1);
        elStrings.put("tx_test_key", new LocaleData.StringInfo("test ελ tx 2"));

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(1);
        translationMap.put("el", elStrings);

        return translationMap;
    }

    @Test
    public void testConstructor_twoProviders_callInternalCacheUpdateInSpecificOrder() {
        TxTranslationsProvider provider1 = new TxTranslationsProvider() {

            @Override
            public LocaleData.TranslationMap getTranslations() {
                return getElTranslationMap1();
            }
        };

        TxTranslationsProvider provider2 = new TxTranslationsProvider() {

            @Override
            public LocaleData.TranslationMap getTranslations() {
                return getElTranslationMap2();
            }
        };

        TxCache internalCache = mock(TxCache.class);
        TxTranslationsProvider[] providers = new TxTranslationsProvider[]{provider1, provider2};
        TxProviderBasedCache providerBasedCache = new TxProviderBasedCache(providers, internalCache);

        // We check if the internal cache's update() was called exactly 2 times, passing provider1
        // content in the first time and provider2 content the second time.
        verify(internalCache, times(2)).update(any(LocaleData.TranslationMap.class));
        InOrder orderVerifier = inOrder(internalCache);
        orderVerifier.verify(internalCache, times(1)).update(getElTranslationMap1());
        orderVerifier.verify(internalCache, times(1)).update(getElTranslationMap2());
        orderVerifier.verifyNoMoreInteractions();
    }
}