package com.transifex.common;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class PluralsTest {

    @Test
    public void testFromICUString_normal() {
        String icuString = "{???, plural, zero {task0} one {task} two {task2} few {tasks} many {tasksss} other {tasks}}";
        Plurals plurals = Plurals.fromICUString(icuString);

        assertThat(plurals).isNotNull();
        assertThat(plurals.zero).isEqualTo("task0");
        assertThat(plurals.one).isEqualTo("task");
        assertThat(plurals.two).isEqualTo("task2");
        assertThat(plurals.few).isEqualTo("tasks");
        assertThat(plurals.many).isEqualTo("tasksss");
        assertThat(plurals.other).isEqualTo("tasks");
    }

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

    @Test
    public void testFromICUString_emptyString_returnEmptyPlurals() {
        String icuString = "";
        Plurals plurals = Plurals.fromICUString(icuString);

        assertThat(plurals).isNotNull();
        assertThat(plurals.zero).isNull();
        assertThat(plurals.one).isNull();
        assertThat(plurals.two).isNull();
        assertThat(plurals.few).isNull();
        assertThat(plurals.many).isNull();
        assertThat(plurals.other).isNull();
    }

    @Test
    public void testBuilder_normal() {
        Plurals.Builder sb = new Plurals.Builder();
        sb.setZero("none")
                .setOne("just one")
                .setTwo("just two")
                .setFew("a few")
                .setMany("many")
                .setOther("other!");
        Plurals plurals = sb.buildString();

        assertThat(plurals.zero).isEqualTo("none");
        assertThat(plurals.one).isEqualTo("just one");
        assertThat(plurals.two).isEqualTo("just two");
        assertThat(plurals.few).isEqualTo("a few");
        assertThat(plurals.many).isEqualTo("many");
        assertThat(plurals.other).isEqualTo("other!");
    }

    @Test
    public void testBuilderSetPlural_normal() {
        Plurals.Builder sb = new Plurals.Builder();
        sb.setPlural(Plurals.PluralType.FEW, "test");
        Plurals plurals = sb.buildString();

        assertThat(plurals.zero).isNull();
        assertThat(plurals.one).isNull();
        assertThat(plurals.two).isNull();
        assertThat(plurals.few).isEqualTo("test");
        assertThat(plurals.many).isNull();
        assertThat(plurals.other).isNull();
    }

    @Test
    public void testBuilderSetPlural_nonSupportedPluralType_throwException() {
        Plurals.Builder sb = new Plurals.Builder();

        assertThrows(Plurals.Builder.NonSupportedPluralTypeException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                sb.setPlural("Invalid plural type", "test");
            }
        });
    }

    @Test
    public void testToICUString_normal() {
        Plurals.Builder sb = new Plurals.Builder();
        sb.setZero("none")
                .setOne("just one")
                .setTwo("just two")
                .setFew("a few")
                .setMany("many")
                .setOther("other!");
        Plurals plurals = sb.buildString();
        
        String icuString = plurals.toICUString();

        assertThat(icuString).isEqualTo("{cnt, plural, zero {none} one {just one} two {just two} few {a few} many {many} other {other!}}");
    }

    @Test
    public void testToICUString_empty() {
        Plurals.Builder sb = new Plurals.Builder();
        Plurals plurals = sb.buildString();

        String icuString = plurals.toICUString();

        assertThat(icuString).isEqualTo("{cnt, plural,}");
    }

    @Test
    public void testGetPlural_normal() {
        Plurals.Builder sb = new Plurals.Builder();
        sb.setZero("none")
                .setOne("just one")
                .setTwo("just two")
                .setFew("a few")
                .setMany("many")
                .setOther("other!");
        Plurals plurals = sb.buildString();

        assertThat(plurals.getPlural(Plurals.PluralType.ZERO)).isEqualTo("none");
        assertThat(plurals.getPlural(Plurals.PluralType.ONE)).isEqualTo("just one");
        assertThat(plurals.getPlural(Plurals.PluralType.TWO)).isEqualTo("just two");
        assertThat(plurals.getPlural(Plurals.PluralType.FEW)).isEqualTo("a few");
        assertThat(plurals.getPlural(Plurals.PluralType.MANY)).isEqualTo("many");
        assertThat(plurals.getPlural(Plurals.PluralType.OTHER)).isEqualTo("other!");
    }

    @Test
    public void testGetPlural_nonSupportedPluralType_throwException() {
        Plurals.Builder sb = new Plurals.Builder();
        sb.setZero("none")
                .setOne("just one")
                .setTwo("just two")
                .setFew("a few")
                .setMany("many")
                .setOther("other!");
        Plurals plurals = sb.buildString();

        assertThrows(Plurals.Builder.NonSupportedPluralTypeException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                plurals.getPlural("Invalid plural type");
            }
        });

    }
}