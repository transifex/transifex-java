# Transifex Native Android SDK

Transifex Native Android SDK is a collection of tools to easily localize your Android applications 
using [Transifex Native](https://www.transifex.com/native/). The tool can fetch translations 
over the air (OTA) to your apps.

The library's minimum supported SDK is `18` (Android 4.3) and uses [appcompat](https://developer.android.com/jetpack/androidx/releases/appcompat) `1.2.0`.

Learn more about [Transifex Native](https://docs.transifex.com/transifex-native-sdk-overview/introduction).

//TODO:
You can find the SDK's documentation here.

## Usage

The SDK allows you to keep using the same string methods that Android 
provides, such as `getString(int id)`, `getText(int id)`, etc, but at the same time taking 
advantage of the features that Transifex Native offers, such as OTA translations.

### SDK installation

//TODO: write about gradle dependency

### SDK configuration 

Configure the SDK in your `Application` class. The language codes supported by Transifex can be found [here](https://www.transifex.com/explore/languages/). Keep in mind that in the sample code below you will have to replace 
`<transifex_token>` with the actual token that is associated with your Transifex project and resource.

```java
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize TxNative
        String token = "<transifex_token>";

        LocaleState localeState = new LocaleState(getApplicationContext(),
                "en",
                new String[]{"en", "el", "de", "fr", "ar", "sl"},
                null);
        
        TxNative.init(getApplicationContext(), localeState, token, null, null, null);

        // Fetch all translations from CDS
        TxNative.fetchTranslations(null);
     }
```

If you want to enable [multilingual support](https://developer.android.com/guide/topics/resources/multilingual-support.html) starting from Android N, place the supported app languages in your app's gradle file:

```gradle
android {
    // ...
    defaultConfig {

        resConfigs "en", "el", "de", "fr", "ar", "sl"

    }
```

You can also specify languages (in both the `LocaleState` and your app's gradle file) followed by the regional code:

```java
    @Override
    public void onCreate() {
        // ...	
        LocaleState localeState = new LocaleState(getApplicationContext(),
                "en",
                new String[]{"en", "es_SP", "es_MX"},
                null);
        
     }
```

The SDK's functionality is enabled by wrapping the context before calling any [String resource](https://developer.android.com/reference/android/content/res/Resources#getString(int,%20java.lang.Object...)) related methods.

If your activity doesn't extend `AppCompatActivity`, use the following code or have your activity extend a base class:

```java
public class BaseActivity extends Activity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(TxNative.wrap(base));
    }

}
```

If your activity extends `AppCompatActivity` activity. use the following code or have your activity extend a base class:
```java
public class BaseAppCompatActivity extends AppCompatActivity {

    private TxContextWrappingDelegate mAppCompatDelegate;
    private Resources mResources;

    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        if (mAppCompatDelegate == null) {
            mAppCompatDelegate = new TxContextWrappingDelegate(super.getDelegate());
        }
        return mAppCompatDelegate;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public Resources getResources() {
        if (mResources == null && VectorEnabledTintResources.shouldBeUsed()) {
            mResources = new VectorEnabledTintResourcesWrapper(this, getBaseContext().getResources());
        }
        return mResources == null ? super.getResources() : mResources;
    }
}
```

If you want to use the SDK outside an activity's context, such as a service context, make sure that you wrap the context:

```java
public class SimpleIntentService extends JobIntentService {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TxNative.generalWrap(newBase));
    }
}
```

If you want to disable the SDK functionality, don't initialize it and don't call any `TxNative` methods. The wrapping methods can still be called but they will be a no-op and the context will not be wrapped.

### Fetching translations

As soon as `fetchTranslations` is called, the SDK will attempt to download the 
translations for all locales - except for the source locale - that are defined in the 
initialization of `TxNative`. 

For the moment, the translations are stored only in memory and are available for the 
current app session. In later versions of the SDK, the translations will also be stored on 
the device and will be available for subsequent app sessions.


### Invalidating CDS cache

The cache of CDS has a TTL of 30 minutes. If you update some translations on Transifex 
and you need to see them on your app immediately, you need to make an HTTP request 
to the [invalidation endpoint](https://github.com/transifex/transifex-delivery/#invalidate-cache) of CDS.

## Sample app

You can see the SDK used and configured in more advanced ways in the provided sample app.

## License
Licensed under Apache License 2.0, see [LICENSE](LICENSE) file.