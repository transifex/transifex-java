package com.transifex.txnative.missingpolicy;

import org.junit.Test;

import java.util.Date;

import static com.google.common.truth.Truth.assertThat;

public class PseudoTranslationPolicyTest {

    final int stringId = 0;
    final String stringResourceName = "dummy_name";
    final String locale = "el";

    @Test
    public void testGet() {
        String sourceString = "The quick\n brown fox \nένα!";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.get(sourceString, stringId, stringResourceName, locale);

        assertThat(translated).isEqualTo("Ťȟê ʠüıċǩ\n ƀȓøẁñ ƒøẋ \nένα!");
    }

    @Test
    public void testGet_stringFormat() {
        String sourceString = "This is a %s %B test";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.get(sourceString, stringId, stringResourceName, locale);

        assertThat(translated).isEqualTo("Ťȟıš ıš à %s %B ťêšť");
    }

    @Test
    public void testGet_stringFormat2() {
        String sourceString = "This is a %32.12f test";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.get(sourceString, stringId, stringResourceName, locale);

        assertThat(translated).isEqualTo("Ťȟıš ıš à %32.12f ťêšť");
    }

    @Test
    public void testGet_stringFormat3() {
        String sourceString = "This is a |%010d| test";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.get(sourceString, stringId, stringResourceName, locale);

        assertThat(translated).isEqualTo("Ťȟıš ıš à |%010d| ťêšť");
    }

    @Test
    public void testGet_stringFormatWithDate() {
        String sourceString = "This is a %tM test";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.get(sourceString, stringId, stringResourceName, locale);

        assertThat(translated).isEqualTo("Ťȟıš ıš à %tM ťêšť");
    }
}