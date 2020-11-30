package com.transifex.txnative.wrappers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

import com.transifex.txnative.TxResources;

/**
 * Context wrapper that enables TxNative functionality by wrapping the base resources with
 * {@link TxResources}.
 */
public class TxContextWrapper extends ContextWrapper {

    private Resources mResources;

    public TxContextWrapper(Context base) {
        super(base);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public Resources getResources() {
        if (mResources != null) {
            return  mResources;
        }

        mResources = new TxResources(super.getResources());

        return mResources;
    }
}