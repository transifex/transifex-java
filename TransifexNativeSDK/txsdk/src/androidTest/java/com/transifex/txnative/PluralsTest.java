package com.transifex.txnative;

import com.transifex.common.Plurals;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PluralsTest {

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
