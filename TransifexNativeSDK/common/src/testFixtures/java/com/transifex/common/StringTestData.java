package com.transifex.common;

import java.util.HashMap;

public class StringTestData {

    public static LocaleData.LocaleStrings getElLocaleStrings() {
        HashMap<String, LocaleData.StringInfo> map = new HashMap<>();
        map.put("test_key", new LocaleData.StringInfo("Καλημέρα"));
        map.put("test_key3", new LocaleData.StringInfo(""));
        return new LocaleData.LocaleStrings(map);
    }

    public static LocaleData.LocaleStrings getEsLocaleStrings() {
        HashMap<String, LocaleData.StringInfo> map = new HashMap<>();
        map.put("test_key", new LocaleData.StringInfo("Buenos días"));
        map.put("test_key3", new LocaleData.StringInfo(""));
        return new LocaleData.LocaleStrings(map);
    }

    public static LocaleData.TranslationMap getElEsTranslationMap() {
        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(2);
        translationMap.put("el", getElLocaleStrings());
        translationMap.put("es", getEsLocaleStrings());
        return translationMap;
    }

    public static LocaleData.TranslationMap getElTranslationMap() {
        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(1);
        translationMap.put("el", getElLocaleStrings());
        return translationMap;
    }

    public static LocaleData.TranslationMap getEsTranslationMap() {
        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(1);
        translationMap.put("es", getElLocaleStrings());
        return translationMap;
    }
}
