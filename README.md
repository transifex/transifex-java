# Transifex Native Android SDK

Transifex Native Android SDK is a collection of tools to easily localize your Android applications 
using [Transifex Native](https://www.transifex.com/native/). The library can fetch translations 
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

Configure the SDK in your `Application` class. 

The language codes supported by Transifex can be found [here](https://www.transifex.com/explore/languages/). They can either use 2 characters, such as `es`, or specify the regional code as well, such as `es_ES`. Keep in mind that in the sample code below you will have to replace `<transifex_token>` with the actual token that is associated with your Transifex project and resource.

```java
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize TxNative
        String token = "<transifex_token>";

        LocaleState localeState = new LocaleState(getApplicationContext(),
                "en",                                                                 // source locale
                new String[]{"en", "el", "de", "fr", "ar", "sl", "es_ES", "es_MX"},   // supported locales
                null);
        
        TxNative.init(
                getApplicationContext(),   // application context
                localeState,               // a LocaleState instance
                token,                     // token
                null,                      // cdsHost URL
                null,                      // a TxCache implementation
                null);                     // a MissingPolicy implementation

        // Fetch all translations from CDS
        TxNative.fetchTranslations(null);
     }
```

In this example, the SDK uses its default cache, `TxStandardCache`, and missing policy, `SourceStringPolicy`. However, you can choose between different cache and missing policy implementations or even provide your own. You can read more on that later.

If you want to enable [multilingual support](https://developer.android.com/guide/topics/resources/multilingual-support.html) starting from Android N, place the supported app languages in your app's gradle file:

```gradle
android {
    ...
    defaultConfig {

        resConfigs "en", "el", "de", "fr", "ar", "sl", "es_ES", "es_MX"

    }
```

### Context Wrapping 

The SDK's functionality is enabled by wrapping the context, so that all string resource related methods, such a [`getString()`](https://developer.android.com/reference/android/content/res/Resources#getString(int,%20java.lang.Object...)), [`getText()`](https://developer.android.com/reference/android/content/res/Resources#getText(int)), flow through the SDK.

To enable context wrapping in your activity, use the following code or have your activity extend a base class:

```java
public class BaseActivity extends Activity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(TxNative.wrap(base));
    }

}
```

If your activity extends `AppCompatActivity` activity, use the following code or have your activity extend a base class:
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

If you want to use the SDK in some arbitrary place where you can get your application's context, please do the following:

```java
    ...
    // Wrap the context
    Context wrappedContext = TxNative.generalWrap(getApplicationContext()); 
    // Use the wrapped context for getting a string
    wrappedContext.getString();                                      
```

If you want to disable the SDK functionality, don't initialize it and don't call any `TxNative` methods. `TxNative.wrap()` and `TxNative.generalWrap()` will be a no-op and the context will not be wrapped. Thus, all `getString()` etc methods, won't flow through the SDK.

### Fetching translations

As soon as `fetchTranslations()` is called, the SDK will attempt to download the 
translations for all locales - except for the source locale - that are defined in the 
initialization of `TxNative`. 

The `fetchTranslations()` method in the previous example is called as soon as the application launches, but that's not required. Depending on the application, the developer might choose to call that method whenever it is most appropriate (for example, each time the application is brought to the foreground or when the internet connectivity is established).

### Invalidating CDS cache

The cache of CDS has a TTL of 30 minutes. If you update some translations on Transifex 
and you need to see them on your app immediately, you need to make an HTTP request 
to the [invalidation endpoint](https://github.com/transifex/transifex-delivery/#invalidate-cache) of CDS.

### Standard Cache

The default cache strategy used by the SDK, if no other cache is provided by the developer, is the `TxStandardCache.getCache()`. The standard cache operates by making use of the publicly exposed classes and interfaces from the `com.transifex.txnative.cache` package of the SDK, so it's easy to construct another cache strategy if that's desired.

The standard cache is initialized with a memory cache that manages all cached entries in memory. When the memory cache gets initialized, it tries to look up if there are any already stored translations in the file system:

* First, it looks for translations saved in the app's [Assets directory](https://developer.android.com/reference/android/content/res/AssetManager) that may have been offered by the developer, using the command-line tool when building the app.
* Secondly, it looks for translations in the app's [internal cache directory](https://developer.android.com/training/data-storage/app-specific#internal-create-cache), in case the app had already downloaded the translations from the server from a previous launch. These translations take precedence over the previous ones, if found.

Whenever new translations are fetched from the server using the `fetchTranslations()` method, the standard cache is updated and those translations are stored as-is in the app's cache directory, in the same directory used previsouly during initialization. The in-memory cache though is not affected by the update. An app restart is required to read the newly saved translations.

#### Alternative cache strategy

The SDK allows you to implement your own cache from scratch by implementing the `TxCache` interface. Alternatively, you may change the standard cache strategy by implementing your own using the SDK's publicly exposed classes.

In order to achieve that, you can create a a method that returns a `TxCache` instance, just like in the `TxStandardCache.getCache()` case. For example, the standard cache is created as follows:

```
return new TxFileOutputCacheDecorator(
                <cached Translations Directory>,
                new TXReadonlyCacheDecorator(
                        new TxProviderBasedCache(
                                <providers array>,
                                new TxUpdateFilterCache(
                                        <update policy>,
                                        new TxMemoryCache()
                                )
                        )
                )
        );
```

If you want to have your memory cache updated with the new translations when `fetchTranslations()` is called, you can remove the `TXReadonlyCacheDecorator`.

## Sample app

You can see the SDK used and configured in more advanced ways in the provided sample app.

## Transifex Command Line Tool

Transifex Command Line Tool is a command line tool that can assist developers in pushing the source strings of an Android app to Transifex.

### Building

To build the tool, enter the `TransifexNativeSDK` directory and run from the command line:

```
gradlew :clitool:assemble
```

You can find the generated ".jar" file at `clitool/build/libs/transifex.jar`. You can copy it wherever you want.

### Running

To run the tool, type:
```
java -jar /path/to/transifex.jar
```
, where `/path/to/` is the path to the directory you placed "transifex.jar".

Note that even though the tool uses UTF-8 internally, it's recommended to have your JVM's default character encoding set to UTF-8. If this isn't the case for your system, you can use:
```
java -jar -Dfile.encoding=UTF8 /path/to/transifex.jar
```

For simplicity, the following commands will not include the `java -jar` part required to run the file.

### Usage

To use the tool on your app's Android Studio project, enter the root directory of your project from the command line.

#### Help

`transifex`, `transifex -h`, `transifex --help`
Displays a help dialog with all the options and commands.

`transifex help <command>`
Get help for a particular command.

#### Pushing

`transifex push -t <transifex_token> -s <transifex_secret> -m <app_module_name>`
Pushes the source strings of your app found in a module named "app_module_name". The tool reads the `strings.xml` resource file found in the main source set of the specified module: `app_module_name/src/main/res/values/strings.xml`. It processes it and pushes the result to the Transifex CDS. 

`transifex push -t <transifex_token> -s <transifex_secret> -f path/to/strings1.xml path2/to/strings2.xml`
If your app has a more complex string setup, you can specify one or more string resource files.

`transifex clear -t <transifex_token> -s <transifex_secret>`
Clears all existing resource content from CDS. This action will also remove existing localizations.

#### Pulling

`transifex pull -t <transifex_token> -m <app_module_name> -l <locale>...`
Downloads the translations from Transifex CDS for the specified locales and stores them in txstrings.json files under the "assets" directory of the main source set of the specified app module: `app_module_name/src/main/assets/txnative`. The directory is created if needed. These files will be bundled inside your app and accessed by TxNative.

`transifex pull -t <transifex_token> -d <directory>`
If you have a different setup, you can enter the path to your app's `assets` directory.

## Advanced topics

### Disable TxNative for specific strings

There are cases where you don't want TxNative to interfere with string loading. For example, many apps have API keys or some configuration saved in non-translatable strings in their `strings.xml` file. A method like `getString()` is used to retreive the strings. If you are using the SDK's default missing policy, `SourceStringPolicy`, the expected string will be returned. If, however, you are using some other policy, the string may be altered and your app will not behave as expected. In such a case, make sure that you are using a non-wrapped context when loading such a string:

```java
    getApplicationContext().getString(<string_ID>);
```

### TxNative and 3rd party libraries

Some libs may containt their own localized strings, views or activities. In such as case, you don't want TxNative to interfere with string loading. To accomplish that, make sure that you pass a non-wrapped context to the library's initialization method:

```java
    SomeSDK.init(getApplicationContext());
```

Note however that if a `View` provided by the library is used inside your app's activity, `TxNative` will be used during that view's inflation (if your activity is set up correctly). In that case, any library strings will not be found in TxNative translations and the result will depend on the missing policy used. `SourceStringPolicy` will return the source string provided by the library, which will probably be in english. Using, `AndroidMissingPolicy` will return the localized string using the library's localized string resources, as expected.

### Multiple private libraries

If your app is split into libraries that contain localized strings, views or activities, you need to set up your project in the following way to take advantage of TxNatve in said libs.

The strings included in the libraries have to be pushed to the CDS. You can push all of them at once using the push CLI command, e.g.
`transifex push -t <transifex_token> -s <transifex_secret> -f path/to/strings1.xml path2/to/strings2.xml`. Alternatively, you can push each one separately as long as all strings reach the CDS resource used by the main app.

If your lib has an initialization method, make sure that your main app passes the wrapped context:

```java
    YourLib.init(TxNative.wrap(getApplicationContext()));
```

The following string operations will result in string rendering through TxNative:
* The main app uses, in a layout or programmatically, the strings that the lib provides.
* The lib calls string resource methods such as `getString()`, e.g. for logging or something else, using the context that the main app passes through initialization.
* The lib has views that reference strings via layout or code and the main app displays these views in its activities.

Note that if the main app starts any activity provided by the lib, string rendering won't go through TxNative. If you want to achieve this, you will have to integrate TxNative in the lib by following these steps: 
1. Use TxNative as a dependency in your library.
2. Implement TxNative in the lib's activities.
3. Note that TxNative should not be initialized inside the lib. The main app is responsible for this.

## License
Licensed under Apache License 2.0, see [LICENSE](LICENSE) file.