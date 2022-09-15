package com.transifex.txnative.activity;

import android.app.Activity;
import android.content.Context;

import com.transifex.txnative.TxNative;

/**
 * A base activity that implements context wrapping so that the TxNative functionality is enabled.
 * <p>
 * Make sure your activities extend this class or have the same implementation.
 * <p>
 * If your app uses <code>Appcompat</code> don't use this class. Instead use
 * {@link TxBaseAppCompatActivity}.
 */
public class TxBaseActivity extends Activity {


    @Override
    protected void attachBaseContext(Context base) {
        // Wrap the Activity context
        super.attachBaseContext(TxNative.wrap(base));
    }

}
