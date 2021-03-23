package com.transifex.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

/**
 * An object representation of Android's
 * <a href="https://developer.android.com/guide/topics/resources/string-resource#Plurals">plurals</a>.
 *
 */
public class Plurals {

    /**
     * A tag that represents the plural type. Can be  {@link #ZERO}, {@link #ONE}, {@link #TWO},
     * {@link #FEW}, {@link #MANY}, {@link #OTHER}.
     */
    @StringDef({PluralType.ZERO, PluralType.ONE, PluralType.TWO, PluralType.FEW, PluralType.MANY, PluralType.OTHER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PluralType {
        String ZERO = "zero";
        String ONE = "one";
        String TWO = "two";
        String FEW = "few";
        String MANY = "many";
        String OTHER = "other";
    }

    private static final Pattern pattern = Pattern.compile("(zero|one|two|few|many|other)\\s*\\{([^}]*)\\}");

    public final String zero;
    public final String one;
    public final String two;
    public final String few;
    public final String many;
    public final String other;

    Plurals(@Nullable String zero, @Nullable String one, @Nullable String two,
            @Nullable String few, @Nullable String many, @Nullable String other) {
        this.zero = zero;
        this.one = one;
        this.two = two;
        this.few = few;
        this.many = many;
        this.other = other;
    }

    /**
     * Returns the plural string for the provided plural type.
     *
     * @param pluralType The plural type of the string.
     *
     * @return The plural string for the provided plural type; the value will be <code>null</code>
     * if no string exists for the provided plural
     */
    public @Nullable String getPlural(@NonNull @PluralType String pluralType) {
        switch (pluralType) {
            case PluralType.ZERO:
                return  zero;
            case PluralType.ONE:
                return one;
            case PluralType.TWO:
                return two;
            case PluralType.FEW:
                return few;
            case PluralType.MANY:
                return many;
            case PluralType.OTHER:
                return other;
            default:
                throw new Builder.NonSupportedPluralTypeException("Plural type '" + pluralType + "' is not supported");
        }
    }

    /**
     * Returns the object as a string in ICU format.
     */
    public @NonNull String toICUString() {
        StringBuilder sb = new StringBuilder("{cnt, plural,");
        if (zero != null) {
            appendPlural(sb, PluralType.ZERO, zero);
        }
        if (one != null) {
            appendPlural(sb, PluralType.ONE, one);
        }
        if (two != null) {
            appendPlural(sb, PluralType.TWO, two);
        }
        if (few != null) {
            appendPlural(sb, PluralType.FEW, few);
        }
        if (many != null) {
            appendPlural(sb, PluralType.MANY, many);
        }
        if (other != null) {
            appendPlural(sb, PluralType.OTHER, other);
        }
        sb.append("}");

        return sb.toString();
    }

    /**
     * Parses the provided ICU string and creates a new Plurals object.
     *
     * @param icuString The ICU string to parse.
     *
     * @return Returns a {@link Plurals} object if parsing was successful; <code>null</code>
     * otherwise
     */
    public static @Nullable
    Plurals fromICUString(@NonNull String icuString) {
        Matcher matcher = pattern.matcher(icuString);
        Builder sb = new Builder();

        while (matcher.find()) {
            String pluralType = matcher.group(1);
            String string = matcher.group(2);
            try {
                sb.setPlural(pluralType, string);
            }
            catch (Builder.NonSupportedPluralTypeException e) {
                return null;
            }
        }

        return sb.buildString();
    }

    private static void appendPlural(@NonNull StringBuilder sb, @NonNull @PluralType String pluralType, @NonNull String string) {
        sb.append(" ").append(pluralType).append(" {").append(string).append("}");
    }

    @Override
    public String toString() {
        return "Plurals{" +
                "zero='" + zero + '\'' +
                ", one='" + one + '\'' +
                ", two='" + two + '\'' +
                ", few='" + few + '\'' +
                ", many='" + many + '\'' +
                ", other='" + other + '\'' +
                '}';
    }

    /**
     * A class for building a {@link Plurals} object.
     */
    public static class Builder {

        public static class NonSupportedPluralTypeException extends RuntimeException{
            public NonSupportedPluralTypeException() {
                super();
            }

            public NonSupportedPluralTypeException(String message) {
                super(message);
            }
        }

        private String zero;
        private String one;
        private String two;
        private String few;
        private String many;
        private String other;

        public Builder setZero(@Nullable String zero) {
            this.zero = zero;
            return this;
        }

        public Builder setOne(@Nullable String one) {
            this.one = one;
            return this;
        }

        public Builder setTwo(@Nullable String two) {
            this.two = two;
            return this;
        }

        public Builder setFew(@Nullable String few) {
            this.few = few;
            return this;
        }

        public Builder setMany(String many) {
            this.many = many;
            return this;
        }

        public Builder setOther(@Nullable String other) {
            this.other = other;
            return this;
        }

        /**
         * Assigns the given string to the given plural type.
         *
         * @param pluralType The plural type of the string.
         * @param string The string to set.
         *
         * @throws NonSupportedPluralTypeException if <code>pluralType</code>'s value is not one of
         * {@link PluralType} ones.
         */
        public Builder setPlural(@NonNull @PluralType String pluralType, @Nullable String string) {
            switch (pluralType) {
                case PluralType.ZERO:
                    setZero(string);
                    break;
                case PluralType.ONE:
                    setOne(string);
                    break;
                case PluralType.TWO:
                    setTwo(string);
                    break;
                case PluralType.FEW:
                    setFew(string);
                    break;
                case PluralType.MANY:
                    setMany(string);
                    break;
                case PluralType.OTHER:
                    setOther(string);
                    break;
                default:
                    throw new NonSupportedPluralTypeException("Plural type '" + pluralType + "' is not supported");
            }

            return this;
        }

        /**
         * Builds a <code>Plurals</code> object with the current configuration.
         */
        public @NonNull
        Plurals buildString() {
            return new Plurals(zero, one, two, few, many, other);
        }
    }
}
