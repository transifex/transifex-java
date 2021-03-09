package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TxReadonlyCacheDecoratorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testUpdate_notCallInternalUpdate() {
        TxMemoryCache internalCache = mock(TxMemoryCache.class);
        TxReadonlyCacheDecorator readOnlyCache = new TxReadonlyCacheDecorator(internalCache);

        // Make sure that the internal cache's update() was not called after calling the readOnlyCache's
        // update
        LocaleData.TranslationMap map = new LocaleData.TranslationMap(0);
        readOnlyCache.update(map);
        verify(internalCache, times(0)).update(map);
    }

    @Test
    public void testGet_callInternalGet() {
        TxMemoryCache internalCache = mock(TxMemoryCache.class);
        TxReadonlyCacheDecorator readOnlyCache = new TxReadonlyCacheDecorator(internalCache);

        // Make sure that the internal cache's get() was called after calling the readOnlyCache's
        // get()
        readOnlyCache.get();
        verify(internalCache, times(1)).get();
    }


}