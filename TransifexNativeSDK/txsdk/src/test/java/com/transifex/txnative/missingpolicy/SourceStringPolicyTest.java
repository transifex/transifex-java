package com.transifex.txnative.missingpolicy;

import android.content.res.Resources;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

public class SourceStringPolicyTest {

    final int stringId = 0;
    final String stringResourceName = "dummy_name";
    final String locale = "el";
    private Resources resources;

    @Before
    public void setUp() {
        resources = mock(Resources.class);
    }

    @Test
    public void testGet() {
        SourceStringPolicy sourceStringPolicy = new SourceStringPolicy();

        assertThat(sourceStringPolicy.get(resources, "test", stringId, stringResourceName, locale))
                .isEqualTo("test");
    }

    @Test
    public void testGetQuantityString() {
        SourceStringPolicy sourceStringPolicy = new SourceStringPolicy();

        assertThat(sourceStringPolicy.getQuantityString(resources, "test", stringId, 1, stringResourceName, locale))
                .isEqualTo("test");
    }
}