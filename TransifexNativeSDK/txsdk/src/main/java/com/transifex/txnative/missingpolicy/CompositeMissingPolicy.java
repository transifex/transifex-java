package com.transifex.txnative.missingpolicy;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

/**
 * Combines multiple policies to create a complex result.
 * <p>
 *  The result of each policy is fed to the next policy as source.
 */
public class CompositeMissingPolicy implements MissingPolicy{

    private final MissingPolicy[] mMissingPolicies;

    /**
     * Creates a new instance with the provided missing policies.
     * <p>
     * The order of the missing policies is important; the result of each policy is fed to the next
     * policy as source.
     *
     * @param missingPolicies The missing policies to be used.
     */
    public CompositeMissingPolicy(@NonNull MissingPolicy[] missingPolicies) {
        mMissingPolicies = missingPolicies;
    }

    /**
     * Returns a string after it has been fed to all of the provided policies from first to last.
     */
    @Override
    @NonNull public CharSequence get(@NonNull CharSequence sourceString, @StringRes int id,
                                     @NonNull String resourceName, @NonNull String locale) {
        CharSequence string = sourceString;
        for (MissingPolicy policy : mMissingPolicies) {
            string = policy.get(string, id, resourceName, locale);
        }

        return  string;
    }

    /**
     * Returns a quantity string after it has been fed to all of the provided policies from first
     * to last.
     */
    @Override
    @NonNull public CharSequence getQuantityString(
            @NonNull CharSequence sourceQuantityString, @PluralsRes int id, int quantity,
            @NonNull String resourceName, @NonNull String locale) {
        CharSequence quantityString = sourceQuantityString;
        for (MissingPolicy policy : mMissingPolicies) {
            quantityString = policy.getQuantityString(quantityString, id, quantity, resourceName, locale);
        }

        return  quantityString;
    }
}
