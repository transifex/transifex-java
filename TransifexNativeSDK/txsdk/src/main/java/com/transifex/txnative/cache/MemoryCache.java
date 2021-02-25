package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A cache that holds translations in memory.
 */
public class MemoryCache implements TxCache {

    private LocaleData.TranslationMap mTranslationMap = new LocaleData.TranslationMap(0);

    @NonNull
    @Override
    public Set<String> getSupportedLocales() {
        return mTranslationMap.getLocales();
    }

    @NonNull
    @Override
    public LocaleData.TranslationMap get() {
        return mTranslationMap;
    }


    @Nullable
    @Override
    public String get(@NonNull String key, @NonNull String locale) {
        LocaleData.LocaleStrings localeStrings = mTranslationMap.get(locale);
        if (localeStrings == null) {
            return  null;
        }

        return localeStrings.get(key);
    }

    @Override
    public void update(@NonNull LocaleData.TranslationMap translationMap) {
        mTranslationMap = translationMap;
    }
}
