package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import androidx.annotation.NonNull;

/**
 * Composite class that accepts a number of translations providers and an internal cache. When
 * initialized, the providers are used to update the internal class in the order they are added in
 * the providers array.
 * <p>
 * Example usage:
 * <pre>
 * TxCache cache = new TxProviderBasedCache(
 *         new TxDiskTranslationsProvider[]{
 *                 new TxDiskTranslationsProvider(firstTranslationsDirectory),
 *                 new TxDiskTranslationsProvider(secondTranslationsDirectory)},
 *         new TxMemoryCache());
 * </pre>
 */
public class TxProviderBasedCache extends TxDecoratorCache {

    /**
     * Creates a provider-based cache with the given internal cache and updates it with with the
     * contents of the given translations providers.
     * <p>
     * The translations providers update the internal cache in the given order. If they return a
     * <code>null</code> or empty {@link LocaleData.TranslationMap} they are
     * ignored.
     * <p>
     * The providers' content is accessed synchronously in the class's constructor.
     *
     * @param providers An array of translations providers.
     * @param internalCache The internal cache to be used.
     */
    public TxProviderBasedCache(@NonNull TxTranslationsProvider[] providers, @NonNull TxCache internalCache) {
        super(internalCache);

        for (TxTranslationsProvider provider : providers) {
            LocaleData.TranslationMap translations = provider.getTranslations();
            if (translations != null && !translations.isEmpty()) {
                mInternalCache.update(provider.getTranslations());
            }
        }
    }
}
