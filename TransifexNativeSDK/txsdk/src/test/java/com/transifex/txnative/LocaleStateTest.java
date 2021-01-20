package com.transifex.txnative;

import org.junit.Test;

import java.util.Locale;

import static com.google.common.truth.Truth.assertThat;

public class LocaleStateTest {

    @Test
    public void testSourceLocale_nullSourceLocale() {
        LocaleState localeState = new LocaleState(null, null,
                null,
                new Locale("el"));

        assertThat(localeState.getSourceLocale()).isEqualTo("en");
    }

    @Test
    public void testAppLocales_nullAppLocales() {
        LocaleState localeState = new LocaleState(null, "en",
                null,
                new Locale("el"));

        assertThat(localeState.getAppLocales()).asList().containsExactly(localeState.getSourceLocale());
    }

    @Test
    public void testAppLocales_nullSourceLocale_nullAppLocales() {
        LocaleState localeState = new LocaleState(null, null,
                null,
                new Locale("el"));

        assertThat(localeState.getAppLocales()).asList().containsExactly(localeState.getSourceLocale());
    }

    @Test
    public void testAppLocalesContainSourceLocale_sourceLocaleIncluded() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "el"},
                new Locale("el"));

        assertThat(localeState.getAppLocales()).asList().contains(localeState.getSourceLocale());
    }

    @Test
    public void testAppLocalesContainSourceLocale_sourceLocaleNotIncluded() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"el"},
                new Locale("el"));

        assertThat(localeState.getAppLocales()).asList().contains(localeState.getSourceLocale());
    }

    @Test
    public void testTranslatedLocalesDoNotContainSourceLocale() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "el"},
                new Locale("el"));

        assertThat(localeState.getTranslatedLocales()).asList().doesNotContain(localeState.getSourceLocale());
    }

    @Test
    public void testTranslatedLocalesEmpty_AppLocalesContainOnlySourceLocale() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en"},
                new Locale("el"));

        assertThat(localeState.getTranslatedLocales()).isEmpty();
    }

    @Test
    public void testTranslatedLocalesEmpty_nullAppLocales() {
        LocaleState localeState = new LocaleState(null, "en",
                null,
                new Locale("el"));

        assertThat(localeState.getTranslatedLocales()).isEmpty();
    }

    @Test
    public void testTranslatedLocales_manyAppLocales() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "el", "es"},
                new Locale("el"));

        assertThat(localeState.getTranslatedLocales()).asList().containsExactly("el", "es");
    }

    @Test
    public void testResolvedLocale_exactMatchExists() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "el_GR", "el_CY", "el", "es"},
                new Locale("el", "gr"));

        assertThat(localeState.getResolvedLocale()).isEqualTo("el_GR");
    }

    @Test
    public void testResolvedLocale_parentDialectExists() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "el_CY", "el", "es"},
                new Locale("el", "gr"));

        assertThat(localeState.getResolvedLocale()).isEqualTo("el");
    }

    @Test
    public void testResolvedLocale_childDialectExists() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "el_CY", "es"},
                new Locale("el", "gr"));

        assertThat(localeState.getResolvedLocale()).isEqualTo("el_CY");
    }

    @Test
    public void testResolvedLocale_noMatchExists() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "es"},
                new Locale("el", "gr"));

        assertThat(localeState.getResolvedLocale()).isNull();
    }

    @Test
    public void testIsSourceLocale_same() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "el"},
                new Locale("en", "US"));

        assertThat(localeState.isSourceLocale()).isTrue();
    }

    @Test
    public void testIsSourceLocale_different() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "el"},
                new Locale("el", "GR"));

        assertThat(localeState.isSourceLocale()).isFalse();
    }

    @Test
    public void testIsSourceLocale_resolvedLocaleNull() {
        LocaleState localeState = new LocaleState(null, "en",
                new String[]{"en", "el"},
                new Locale("es", "ES"));

        assertThat(localeState.isSourceLocale()).isFalse();
    }
}