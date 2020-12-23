package com.transifex.txnative;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A cache that holds translations in memory.
 */
public class MemoryCache implements Cache {

    private String mCurrentLocale;
    private LocaleData.LocaleStrings mCurrentLocaleStrings;
    private LocaleData.TranslationMap mTranslationMap;

    @Override
    public void setCurrentLocale(@Nullable String currentLocale) {
        mCurrentLocale = currentLocale;

        updateCurrentTranslations();
    }

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
    public String get(@NonNull String key) {
        if (mCurrentLocaleStrings == null) {
            return null;
        }

        return mCurrentLocaleStrings.get(key);
    }

    @Override
    public void update(@NonNull LocaleData.TranslationMap translationMap) {
        mTranslationMap = translationMap;
        mCurrentLocaleStrings = null;

        updateCurrentTranslations();
    }

    private void updateCurrentTranslations() {
        if (mTranslationMap == null) {
            return;
        }

        mCurrentLocaleStrings = mTranslationMap.get(mCurrentLocale);
    }
}
