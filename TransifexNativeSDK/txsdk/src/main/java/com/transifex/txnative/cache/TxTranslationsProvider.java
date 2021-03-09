package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import androidx.annotation.Nullable;

/**
 * An interface for classes that act as providers of translations (e.g. extracting them from a file)
 */
public interface TxTranslationsProvider {

    /**
     * Returns the translations from the provider.
     *
     * @return A {@link LocaleData.TranslationMap} object or <code>null</code> if an error occurred.
     * The returned map can be empty if an error occurred.
     */
    @Nullable
    LocaleData.TranslationMap getTranslations();
}
