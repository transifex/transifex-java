package com.transifex.txnative.missingpolicy;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class SourceStringPolicyTest {

    final int stringId = 0;
    final String stringResourceName = "dummy_name";
    final String locale = "el";

    @Test
    public void testGet() {
        SourceStringPolicy sourceStringPolicy = new SourceStringPolicy();

        assertThat(sourceStringPolicy.get("test", stringId, stringResourceName, locale))
                .isEqualTo("test");
    }

    @Test
    public void testGetQuantityString() {
        SourceStringPolicy sourceStringPolicy = new SourceStringPolicy();

        assertThat(sourceStringPolicy.getQuantityString("test", stringId, 1, stringResourceName, locale))
                .isEqualTo("test");
    }
}