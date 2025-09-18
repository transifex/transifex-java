package com.transifex.myapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.transifex.txnative.LocaleState;
import com.transifex.txnative.TxNative;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Uncomment to test vector resources when running on older  platforms (< API 21)
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Uncomment to test if everything works when AppCompat changes the resources to force
        // night mode:
        // https://stackoverflow.com/questions/55265834/change-locale-not-work-after-migrate-to-androidx/58004553#58004553
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Start a service just for testing purposes
        Intent serviceIntent = new Intent(this, SimpleIntentService.class);
        SimpleIntentService.enqueueWork(this, serviceIntent);

        // Uncomment to use strings as served by Android prefixed with "test: "
//        TxNative.setTestMode(true);

        // Uncomment, to disable styling of strings with HTML markup such as
        // R.string.styled_text_not_escaped
//        TxNative.setSupportSpannable(false);

        // Fetch all translations from CDS
        TxNative.fetchTranslations(null, null);
    }

    @Override
    protected void attachBaseContext(Context base) {
        // Initialize TxNative
        String token = null;

        // The app locales entered here should match the ones in `resConfigs` in gradle, so that
        // multi locale support works for newer Androids.
        LocaleState localeState = new LocaleState(base,
                "en",
                new String[]{"en", "el", "de", "fr", "ar", "sl"},
                null);

        TxNative.init(
                base,                      // application context
                localeState,               // a LocaleState instance
                token,                     // token
                null,                      // cdsHost URL
                null,                      // a TxCache implementation
                null);                     // a MissingPolicy implementation

        // OPTIONAL:
        // Wrap the application's base context to allow TxNative to intercept all string resource
        // requests (e.g. from getApplicationContext().getString()).
        // Warning: This global wrapper can interfere with third-party libraries that use their
        // own string resources. Use "AndroidMissingPolicy" so that these libraries have their
        // strings translated.
        super.attachBaseContext(TxNative.wrap(base));

        // SAFER: Do not wrap the application's base context.
        // super.attachBaseContext(base);
    }
}
