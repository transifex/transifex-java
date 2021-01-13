package com.transifex.txnative.missingpolicy;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class SourceStringPolicyTest {

    @Test
    public void testGet() {
        SourceStringPolicy sourceStringPolicy = new SourceStringPolicy();

        assertThat(sourceStringPolicy.get("test")).isEqualTo("test");
    }

}