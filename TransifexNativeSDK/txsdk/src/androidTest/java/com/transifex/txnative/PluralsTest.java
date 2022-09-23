package com.transifex.txnative;

import com.transifex.common.Plurals;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PluralsTest {

    // The following test is copied from the respective unit test. We just want to make sure the
    // internal regex patter compiles correctly on a device.

    @Test
    public void testFromICUString_oneIncorrectPluralStyle_parseRestOfThem() {
        String icuString = "{???, plural, WRONG {task} other {tasks}}";
        Plurals plurals = Plurals.fromICUString(icuString);

        assertThat(plurals).isNotNull();
        assertThat(plurals.zero).isNull();
        assertThat(plurals.one).isNull();
        assertThat(plurals.two).isNull();
        assertThat(plurals.few).isNull();
        assertThat(plurals.many).isNull();
        assertThat(plurals.other).isEqualTo("tasks");
    }
}
