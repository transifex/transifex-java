package com.transifex.myapplication;

import android.annotation.SuppressLint;
import android.content.res.Resources;

import com.transifex.txnative.wrappers.VectorEnabledTintResourcesWrapper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.TxContextWrappingDelegate;
import androidx.appcompat.widget.VectorEnabledTintResources;

public class BaseAppCompatActivity extends AppCompatActivity {

    private TxContextWrappingDelegate mAppCompatDelegate;
    private Resources mResources;


    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        // Wrap AppCompat delegate
        if (mAppCompatDelegate == null) {
            mAppCompatDelegate = new TxContextWrappingDelegate(super.getDelegate());
        }
        return mAppCompatDelegate;
    }


    // The following is required if "AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);"
    // is used.
    @SuppressLint("RestrictedApi")
    @Override
    public Resources getResources() {
        if (mResources == null && VectorEnabledTintResources.shouldBeUsed()) {
            // We wrap the Resources returned by the base context in VectorEnabledTintResourcesWrapper,
            // similarly to what AppCompatActivity does. However, these resources are not always
            // the same as the ones used internally by AppCompatActivity, but that's the best
            // we can do.
            //TODO: perhaps we could use reflection
            mResources = new VectorEnabledTintResourcesWrapper(this, getBaseContext().getResources());
        }
        return mResources == null ? super.getResources() : mResources;
    }
}
