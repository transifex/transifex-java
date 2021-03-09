package com.transifex.txnative.missingpolicy;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class PseudoTranslationPolicyTest {

    @Test
    public void testGet() {
        String sourceString = "The quick\n brown fox \nένα!";
        PseudoTranslationPolicy policy = new PseudoTranslationPolicy();
        CharSequence translated = policy.get(sourceString);

        assertThat(translated).isEqualTo("Ťȟê ʠüıċǩ\n ƀȓøẁñ ƒøẋ \nένα!");
    }
}