package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import androidx.annotation.NonNull;

/**
 * Decorator class that makes the internal cache read-only so that no update operations are allowed.
 */
public class TxReadonlyCacheDecorator extends TxDecoratorCache {

    public TxReadonlyCacheDecorator(@NonNull TxCache internalCache) {
        super(internalCache);
    }

    /**
     * This method is a no-op as this cache decorator is read-only.
     */
    @Override
    public void update(@NonNull LocaleData.TranslationMap translationMap) {
        // No-op
    }
}
