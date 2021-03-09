package com.transifex.txnative.missingpolicy;

import androidx.annotation.NonNull;

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
    @NonNull public CharSequence get(@NonNull CharSequence sourceString) {
        String string = sourceString.toString();
        for (MissingPolicy policy : mMissingPolicies) {
            string = policy.get(string).toString();
        }

        return  string;
    }
}
