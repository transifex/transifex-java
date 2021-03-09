package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Decorator class managing an internal cache and propagating the get() and update() protocol method
 * calls to said cache. The class should be extended to add new capabilities.
 */
public class TxDecoratorCache implements TxCache {

    protected final TxCache mInternalCache;

    /**
     * Creates a decorator with the provided cache.
     *
     * @param internalCache The cache to be used.
     */
    public TxDecoratorCache(@NonNull TxCache internalCache) {
        mInternalCache = internalCache;
    }

    @NonNull
    @Override
    public LocaleData.TranslationMap get() {
        return mInternalCache.get();
    }

    @Nullable
    @Override
    public String get(@NonNull String key, @NonNull String locale) {
        return mInternalCache.get(key, locale);
    }

    @Override
    public void update(@NonNull LocaleData.TranslationMap translationMap) {
        mInternalCache.update(translationMap);
    }
}
