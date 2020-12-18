package com.transifex.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.transifex.txnative.TxNative;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

/**
 * A JobIntentService that demonstrates how to use TransifexNative in services.
 */
public class SimpleIntentService extends JobIntentService {

    public static final String TAG = SimpleIntentService.class.getSimpleName();

    static final int JOB_ID = 1000;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SimpleIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // Make sure that you use getBaseContext() and not getApplicationContext()
        String success = getBaseContext().getResources().getString(R.string.success);

        Log.d(TAG, "Service status: " + success);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // Wrap the base context
        super.attachBaseContext(TxNative.generalWrap(newBase));
    }
}
