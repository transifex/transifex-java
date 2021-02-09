package com.transifex.txnative;

import com.transifex.common.LocaleData;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A cache that holds translations in memory.
 */
public class MemoryCache implements Cache {

    private LocaleData.TranslationMap mTranslationMap;

    @NonNull
    @Override
    public Set<String> getSupportedLocales() {
        if (mTranslationMap == null) {
            return new HashSet<>(0);
        }

        return mTranslationMap.getLocales();
    }


    @Nullable
    @Override
    public String get(@NonNull String key, @NonNull String locale) {
        if (mTranslationMap == null) {
            return null;
        }

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
