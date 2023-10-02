package com.transifex.txnative.wrappers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

import com.transifex.txnative.NativeCore;
import com.transifex.txnative.TxResources;

import androidx.annotation.NonNull;

/**
 * Context wrapper that enables TxNative functionality by wrapping the base resources with
 * {@link TxResources}.
 */
public class TxContextWrapper extends ContextWrapper {

    private Resources mWrappedResources;
    private final NativeCore mNativeCore;

    public TxContextWrapper(@NonNull Context base, @NonNull NativeCore nativeCore) {
        super(base);
        mNativeCore = nativeCore;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public Resources getResources() {
        // If the base resources point to a new AssetManager object, re-wrap them.
        if (mWrappedResources != null
                && mWrappedResources.getAssets() != super.getResources().getAssets()) {
            mWrappedResources = null;
        }

        if (mWrappedResources == null) {
            mWrappedResources = new TxResources(super.getResources(), mNativeCore);
        }
        return mWrappedResources;
    }
}