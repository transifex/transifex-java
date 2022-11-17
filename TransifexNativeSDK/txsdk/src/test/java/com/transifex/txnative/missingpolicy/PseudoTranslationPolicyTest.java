package com.transifex.txnative.missingpolicy;

import android.content.res.Resources;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

public class PseudoTranslationPolicyTest {

    final int stringId = 0;
    final String stringResourceName = "dummy_name";
    final String locale = "el";

    private Resources resources;

    @Before
    public void setUp() {
        resources = mock(Resources.class);
    }

    @Test
    public void testProcessString() {
        String sourceString = "The quick\n brown fox \nένα!";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.processString(sourceString);

        assertThat(translated).isEqualTo("Ťȟê ʠüıċǩ\n ƀȓøẁñ ƒøẋ \nένα!");
    }

    @Test
    public void testProcessString_formatSpecifiers_notAffected() {
        // Make sure that format specifiers are not affected
        String sourceString = "This is a %s %B test";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.processString(sourceString);

        assertThat(translated).isEqualTo("Ťȟıš ıš à %s %B ťêšť");
    }

    @Test
    public void testProcessString_formatSpecifiers2_notAffected() {
        String sourceString = "This is a %32.12f test";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.processString(sourceString);

        assertThat(translated).isEqualTo("Ťȟıš ıš à %32.12f ťêšť");
    }

    @Test
    public void testProcessString_formatSpecifiers3_notAffected() {
        String sourceString = "This is a |%010d| test";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.processString(sourceString);

        assertThat(translated).isEqualTo("Ťȟıš ıš à |%010d| ťêšť");
    }

    @Test
    public void testProcessString_formatSpecifierWithDate_notAffected() {
        String sourceString = "This is a %tM test";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.processString(sourceString);

        assertThat(translated).isEqualTo("Ťȟıš ıš à %tM ťêšť");
    }

    @Test
    public void testGet() {
        String sourceString = "The quick\n brown fox \nένα!";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.get(resources, sourceString, stringId, stringResourceName, locale);

        assertThat(translated).isEqualTo("Ťȟê ʠüıċǩ\n ƀȓøẁñ ƒøẋ \nένα!");
    }

    @Test
    public void testGetQuantityString() {
        String sourceString = "The quick\n brown fox \nένα!";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.getQuantityString(resources, sourceString, stringId, 1, stringResourceName, locale);

        assertThat(translated).isEqualTo("Ťȟê ʠüıċǩ\n ƀȓøẁñ ƒøẋ \nένα!");
    }
}