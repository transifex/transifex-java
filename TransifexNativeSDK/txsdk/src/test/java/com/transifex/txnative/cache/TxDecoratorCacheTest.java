package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class TxDecoratorCacheTest {

    // In these tests, we check that the internal cache methods are called by TxDecoratorCache as
    // expected.

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testGetAll() {
        TxMemoryCache internalCache = mock(TxMemoryCache.class);

        TxDecoratorCache decoratorCache = new TxDecoratorCache(internalCache);
        decoratorCache.get();

        verify(internalCache, times(1)).get();
    }

    @Test
    public void testGet() {
        TxMemoryCache internalCache = mock(TxMemoryCache.class);

        TxDecoratorCache decoratorCache = new TxDecoratorCache(internalCache);
        decoratorCache.get("key1", "el");

        verify(internalCache, times(1)).get("key1", "el");
    }

    @Test
    public void testUpdate() {
        TxMemoryCache internalCache = mock(TxMemoryCache.class);

        TxDecoratorCache decoratorCache = new TxDecoratorCache(internalCache);
        LocaleData.TranslationMap map = new LocaleData.TranslationMap(0);
        decoratorCache.update(map);

        verify(internalCache, times(1)).update(map);
    }

}