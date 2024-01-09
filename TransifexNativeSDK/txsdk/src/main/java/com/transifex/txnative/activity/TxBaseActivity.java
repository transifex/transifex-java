package com.transifex.txnative.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.transifex.txnative.TxNative;

/**
 * A base activity that implements context wrapping so that the TxNative functionality is enabled.
 * <p>
 * Make sure your activities extend this class or have the same implementation.
 * <p>
 * If your app uses <code>Appcompat</code> don't use this class. Instead use
 * {@link TxBaseAppCompatActivity}.
 */
@Deprecated
class TxBaseActivity extends Activity {


    @Override
    protected void attachBaseContext(Context base) {
        // Wrap the Activity context
        super.attachBaseContext(TxNative.wrap(base));
    }

    @Override
    public Resources getResources() {
        // Calling "getBaseContext().getResources()", instead of "super.getResources()",  returns the
        // resources straight from TxResources and makes sure that the underlying assets are updated.
        // "super.getResources()" returns a cached  resources object which may contain older assets.
        return getBaseContext().getResources();
    }

}
