package com.transifex.txnative;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A cache that holds translations in memory.
 */
public class MemoryCache implements Cache {

    private String mCurrentLocale;
    private JSONObject mCurrentTranslations;
    private HashMap<String, JSONObject> mTranslationMap;

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

        return mTranslationMap.keySet();
    }


    @Nullable
    @Override
    public String get(@NonNull String key) {
        if (mCurrentTranslations == null) {
            return null;
        }

        try {
            return mCurrentTranslations.getJSONObject(key).getString("string");
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public void update(@NonNull HashMap<String, JSONObject> translationMap) {
        mTranslationMap = translationMap;
        mCurrentTranslations = null;

        updateCurrentTranslations();
    }

    private void updateCurrentTranslations() {
        if (mTranslationMap == null) {
            return;
        }

        mCurrentTranslations = mTranslationMap.get(mCurrentLocale);
    }
}
