package com.transifex.txnative.missingpolicy;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import androidx.test.core.app.ApplicationProvider;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeMissingPolicyTest {

    final int stringId = 0;
    final String stringResourceName = "dummy_name";
    final String locale = "el";

    private Resources resources;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        resources = mock(Resources.class);
    }

    @Test
    public void testGet_normal() {
        // Mock a missing policy that returns sourceString + " end"
        // and a missing policy that returns "start " + sourceString + " end2"
        MissingPolicy missingPolicy1 = mock(MissingPolicy.class);
        when(missingPolicy1.get(any(), anyString(), anyInt(), anyString(), anyString()))
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        return args[1] + " end";
                    }
        });
        MissingPolicy missingPolicy2 = mock(MissingPolicy.class);
        when(missingPolicy2.get(any(), anyString(), anyInt(), anyString(), anyString()))
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        return "start " + args[1] + " end2";
            }
        });

        MissingPolicy[] missingPolicies = new MissingPolicy[]{missingPolicy1, missingPolicy2};
        CompositeMissingPolicy compositeMissingPolicy = new CompositeMissingPolicy(missingPolicies);
        String result = compositeMissingPolicy.get(resources, "test", stringId, stringResourceName, locale).toString();

        // Verify that the get() methods of the missing policies were called in order and had the
        // expected arguments
        InOrder inOrder = inOrder(missingPolicy1, missingPolicy2);
        inOrder.verify(missingPolicy1).get(resources, "test", stringId, stringResourceName, locale);
        inOrder.verify(missingPolicy2).get(resources, "test end", stringId, stringResourceName, locale);
        inOrder.verifyNoMoreInteractions();
        // Assert final result
        assertThat(result).isEqualTo("start test end end2");
    }

    @Test
    public void testGet_emptyPolicyArray_returnSourceString() {
        CompositeMissingPolicy compositeMissingPolicy = new CompositeMissingPolicy(new MissingPolicy[]{});
        String result = compositeMissingPolicy.get(resources, "test", stringId, stringResourceName, locale).toString();

        assertThat(result).isEqualTo("test");
    }

    @Test
    public void testGetQuantityString_normal() {
        // Mock a missing policy that returns sourceString + " end"
        // and a missing policy that returns "start " + sourceString + " end2"
        MissingPolicy missingPolicy1 = mock(MissingPolicy.class);
        when(missingPolicy1.getQuantityString(any(), anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        return args[1] + " end";
                    }
                });
        MissingPolicy missingPolicy2 = mock(MissingPolicy.class);
        when(missingPolicy2.getQuantityString(any(), anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        return "start " + args[1] + " end2";
                    }
                });

        MissingPolicy[] missingPolicies = new MissingPolicy[]{missingPolicy1, missingPolicy2};
        CompositeMissingPolicy compositeMissingPolicy = new CompositeMissingPolicy(missingPolicies);
        String result = compositeMissingPolicy.getQuantityString(resources, "test", stringId, 1, stringResourceName, locale).toString();

        // Verify that the get() methods of the missing policies were called in order and had the
        // expected arguments
        InOrder inOrder = inOrder(missingPolicy1, missingPolicy2);
        inOrder.verify(missingPolicy1).getQuantityString(resources, "test", stringId, 1, stringResourceName, locale);
        inOrder.verify(missingPolicy2).getQuantityString(resources, "test end", stringId, 1, stringResourceName, locale);
        inOrder.verifyNoMoreInteractions();
        // Assert final result
        assertThat(result).isEqualTo("start test end end2");
    }


    @Test
    public void testGetQuantityString_emptyPolicyArray_returnSourceString() {
        CompositeMissingPolicy compositeMissingPolicy = new CompositeMissingPolicy(new MissingPolicy[]{});
        String result = compositeMissingPolicy.getQuantityString(resources, "test", stringId, 1, stringResourceName, locale).toString();

        assertThat(result).isEqualTo("test");
    }
}