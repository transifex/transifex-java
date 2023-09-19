# Transifex Native Android SDK

[![CI](https://github.com/transifex/transifex-java/actions/workflows/gradle.yml/badge.svg)](https://github.com/transifex/transifex-java/actions/workflows/gradle.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.transifex.txnative/txsdk?color=32c955)](https://maven-badges.herokuapp.com/maven-central/com.transifex.txnative/txsdk)

Transifex Native Android SDK is a collection of tools to easily localize your Android applications
using [Transifex Native](https://www.transifex.com/native/). The Android library can fetch translations
over the air (OTA) to your apps and the command line tool can upload your app's source strings to Transifex.

Learn more about [Transifex Native](https://developers.transifex.com/docs/native).

The full documentation is available at [https://transifex.github.io/transifex-java/](https://transifex.github.io/transifex-java/).

## Sample app

You can see the SDK used and configured in multiple ways in the provided [sample app](https://github.com/transifex/transifex-java/tree/master/TransifexNativeSDK/app) of this repo.

## Usage

The SDK allows you to keep using the same string methods that Android
provides, such as `getString(int id)`, `getText(int id)`, etc. or use strings in XML layout files, but at the same time taking
advantage of the features that Transifex Native offers, such as OTA translations.

## SDK installation

Include the dependency:

```groovy
implementation 'com.transifex.txnative:txsdk:x.y.z'
```

Please replace `x`, `y` and `z` with the latest version numbers: [![Maven Central](https://img.shields.io/maven-central/v/com.transifex.txnative/txsdk?color=32c955)](https://maven-badges.herokuapp.com/maven-central/com.transifex.txnative/txsdk)


The library's minimum supported SDK is 18 (Android 4.3) and is compatible with [Appcompat](https://developer.android.com/jetpack/androidx/releases/appcompat).

The SDK does not add Appcompat as a dependency. It can work in apps that don't use Appcompat and in apps that use Appcompat.

## SDK configuration

Configure the SDK in your `Application` class.

The language codes supported by Transifex can be found [here](https://explore.transifex.com/languages/). They can either use 2 characters, such as `es`, or specify the regional code as well, such as `es_ES`. Keep in mind that in the sample code below you will have to replace `<transifex_token>` with the actual token that is associated with your Transifex project and resource.

```java
@Override
public void onCreate() {
    super.onCreate();

    // Initialize TxNative
    String token = "<transifex_token>";

    LocaleState localeState = new LocaleState(getApplicationContext(),
        // source locale
        "en",
        // supported locales
        new String[]{"en", "el", "de", "fr", "ar", "sl", "es_ES", "es_MX"},
        null);

    TxNative.init(
        // application context
        getApplicationContext(),
        // a LocaleState instance
        localeState,
        // token
        token,
        // cdsHost URL
        null,
        // a TxCache implementation
        null,
        // a MissingPolicy implementation
        null);

    // Fetch all translations from CDS
    TxNative.fetchTranslations(null, null);
}
```

In this example, the SDK uses its default cache, `TxStandardCache`, and default missing policy, `SourceStringPolicy`. However, you can choose between different cache and missing policy implementations or even provide your own. For example, if you want to fallback to translations provided via `strings.xml` files, use the [`AndroidMissingPolicy`](https://transifex.github.io/transifex-java/com/transifex/txnative/missingpolicy/AndroidMissingPolicy.html). You can read more about cache implementations later on.

In this example, we fetch the translations for all locales. If you want, you can target specific locales or strings that have specific tags.

## App configuration

Starting from Android N, Android has [multilingual support](https://developer.android.com/guide/topics/resources/multilingual-support.html): users can select more that one locale in Android's settings and the OS will try to pick the topmost locale that is supported by the app. If your app makes use of `Appcompat`, place the supported app languages in your app’s gradle file:

```gradle
android {
    ...
    defaultConfig {
        resConfigs "en", "el", "de", "fr", "ar", "sl", "es_ES", "es_MX"
    }
}
```

This will let Android know which locales your app supports and help it choose the correct one in case of a multilingual user.

For some languages such as Kinyarwanda, you will need to do some more work. You should define a dummy string in your default, unlocalized `strings.xml` file and place a `strings.xml` file for that locale and define the same string there. For example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="dummy">dummy</string>
</resources>
```

If you don't do that, Android will never choose that language.

If you don't use `Appcompat` you will have to place a `strings.xml` for all supported locales.

## Context Wrapping

The SDK's functionality is enabled by wrapping the context, so that all string resource related methods, such a [`getString()`](https://developer.android.com/reference/android/content/res/Resources#getString(int,%20java.lang.Object...)), [`getText()`](https://developer.android.com/reference/android/content/res/Resources#getText(int)), flow through the SDK.

To enable context wrapping in your `AppCompatActivity`, extend the SDK's [TxBaseAppcompatActivity](https://transifex.github.io/transifex-java/com/transifex/txnative/activity/TxBaseAppCompatActivity) or copy its [implementation](https://github.com/transifex/transifex-java/blob/master/TransifexNativeSDK/txsdk/src/main/java/com/transifex/txnative/activity/TxBaseAppCompatActivity.java) to your own base class.
If you are using an older `AppCompat` version, please read the class's implementation as you may need to uncomment some code.


If you don't use `AppCompat`, extend the SDK's [TxBaseActivity](https://transifex.github.io/transifex-java/com/transifex/txnative/activity/TxBaseActivity) or copy its [implementation](https://github.com/transifex/transifex-java/blob/master/TransifexNativeSDK/txsdk/src/main/java/com/transifex/txnative/activity/TxBaseActivity.java) to your own base class.

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

## Cache

The SDK relies on a cache mechanism to return source strings and translations for the supported locales. If the cache is empty, the SDK will return a string based on the current [missing policy](https://transifex.github.io/transifex-java/com/transifex/txnative/missingpolicy/MissingPolicy).

The cache is updated when `fetchTranslations()` is called. You can read more on that later.

The cache can be prepopulated by the developer, using the command-line tool's [pull command](#pulling).

### Standard Cache

The default cache strategy used by the SDK, if no other cache is provided by the developer, is returned by [TxStandardCache.getCache()](https://transifex.github.io/transifex-java/com/transifex/txnative/cache/TxStandardCache.html#getCache(android.content.Context,java.lang.Integer,java.io.File)). The standard cache operates by making use of the publicly exposed classes and interfaces from the [com.transifex.txnative.cache](https://transifex.github.io/transifex-java/com/transifex/txnative/cache/package-summary) package of the SDK, so it's easy to construct another cache strategy if that's desired.

The standard cache is initialized with a memory cache that manages all cached entries in memory. When the memory cache gets initialized, it tries to look up if there are any already stored translations in the file system:

* First, it looks for translations saved in the app's [Assets directory](https://developer.android.com/reference/android/content/res/AssetManager) that may have been offered by the developer, using the command-line tool when building the app.
* Secondly, it looks for translations in the app's [internal cache directory](https://developer.android.com/training/data-storage/app-specific#internal-create-cache), in case the app had already downloaded the translations from the server from a previous launch. These translations take precedence over the previous ones, if found.

Whenever new translations are fetched from the server using the `fetchTranslations()` method, the standard cache is updated and those translations are stored as-is in the app's cache directory, in the same directory used previsouly during initialization. **The in-memory cache though is not affected by the update.** An app restart is required to read the newly saved translations.

Under the SDK's default configuration, the first time the app is launched, the source strings are displayed, unless the developer has bundled translations via the command-line tool.

### Alternative cache strategy

The SDK allows you to implement your own cache from scratch by implementing the [TxCache](https://transifex.github.io/transifex-java/com/transifex/txnative/cache/TxCache) interface. Alternatively, you may change the standard cache strategy by implementing your own using the SDK's publicly exposed classes.

In order to achieve that, you can create a a method that returns an object that implements `TxCache`. For example, the standard cache is created as follows (you can see the full source code [here](https://github.com/transifex/transifex-java/blob/master/TransifexNativeSDK/txsdk/src/main/java/com/transifex/txnative/cache/TxStandardCache.java)):

```java
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

## Fetching translations

As soon as [fetchTranslations()](https://transifex.github.io/transifex-java/com/transifex/txnative/TxNative.html#fetchTranslations(java.lang.String,java.util.Set)) is called, the SDK will attempt to download both the source locale strings
and the translations for the supported locales. If successful, it will update the cache.

The `fetchTranslations()` method in the SDK configuration example is called as soon as the application launches, but that's not required. Depending on the application, the developer might choose to call that method whenever it is most appropriate (for example, each time the application is brought to the foreground or when the internet connectivity is established).

## Transifex Command Line Tool

Transifex Command Line Tool is a command line tool that can assist developers in pushing the source strings of an Android app to Transifex.

### Building

You can get the cli tool pre-built from the [release page](https://github.com/transifex/transifex-java/releases).

If you want to build the tool yourself, enter the `TransifexNativeSDK` directory and run from the command line:

```
gradlew :clitool:assemble
```

You will find the built `.jar` file at `clitool/build/libs/transifex.jar`. You can copy it wherever you want.

### Running

To run the tool, type:
```
java -jar /path/to/transifex.jar
```
where `/path/to/` is the path to the directory you placed "transifex.jar".

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

`transifex push -t <transifex_token> -s <transifex_secret> -f path/to/strings1.xml path2/to/strings2.xml ...`
If your app has a more complex string setup, you can specify one or more string resource files.

`transifex push -t <transifex_token> -s <transifex_secret> -m <app_module_name> --dry-run -v`
Append `--dry-run -v` to display the source strings that will be pushed without actually pushing them.

`transifex clear -t <transifex_token> -s <transifex_secret>`
Clears all existing resource content from CDS. This action will also remove existing localizations.

#### Pulling

`transifex pull -t <transifex_token> -m <app_module_name> -l <locale>...`
Downloads the translations from Transifex CDS for the specified locales and stores them in txstrings.json files under the "assets" directory of the main source set of the specified app module: `app_module_name/src/main/assets/txnative`. The directory is created if needed. These files will be bundled inside your app and accessed by TxNative.


`transifex pull -t <transifex_token> -d <directory> -l <locale>...`
If you have a different setup, you can enter the path to your app's `assets` directory.

Note that cache of CDS has a TTL of 30 minutes. If you update some translations on Transifex
and you need to see them on your app immediately or pull them using the above command, you need to make an HTTP request
to the [invalidation endpoint](https://github.com/transifex/transifex-delivery/#invalidate-cache) of CDS.

## Advanced topics

### Disable TxNative for specific strings

There are cases where you don't want TxNative to interfere with string loading. For example, many apps have API keys or some configuration saved in non-translatable strings in their `strings.xml` file. A method like `getString()` is used to retrieve the strings. If you are using the SDK's default missing policy, `SourceStringPolicy`, the expected string will be returned. If, however, you are using some other policy, the string may be altered and your app will not behave as expected. In such a case, make sure that you are using a non-wrapped context when loading such a string:

```java
getApplicationContext().getString(<string_ID>);
```
### String styling

As explained in Android's [documentation](https://developer.android.com/guide/topics/resources/string-resource#StylingWithHTML), strings can have styling applied to them if they contain HTML markup. There are two ways to accomplish that.

Write a string  with HTML markup. For example:

```xml
<string name="styled_text">A <font color="#FF7700">localization</font> platform</string>
```

The SDK will parse the tags into spans so that styling is applied. You can reference such a string in a layout XML file or use `getText()` (not `getString()`) and set it programmatically to the desired view. To disable this behavior and treat tags as plain text, you can disable span support by calling [`TxNative.setSupportSpannable(false)`](https://transifex.github.io/transifex-java/com/transifex/txnative/TxNative.html#setSupportSpannable(boolean)).
Note that when span support is enabled and tags are detected in a string, the SDK uses `fromHTML()`. This has the side-effect of new lines being converted to spaces and sequences of whitespace characters being collapsed into a single space.

Alternatively, you can write a string with the opening brackets escaped (using `&lt;` instead of `<`):

```xml
<string name="styled_text">A &lt;font color="#FF7700">localization&lt;/font> platform</string>
```

Then, you can use [`fromHTML()`](https://developer.android.com/reference/androidx/core/text/HtmlCompat#fromHtml(java.lang.String,int,android.text.Html.ImageGetter,android.text.Html.TagHandler)) to get styled text as shown below:

```java
String string = getResources().getString(R.string.styled_text);
Spanned styledTest = HtmlCompat.fromHtml(string, HtmlCompat.FROM_HTML_MODE_COMPACT);
someView.setText(styledText);
```

### Stylable attributes

Android lets you define attributes that can point to different string resources, according to the current theme. For example you can create an `attr.xml` file that declares a stylable:

```xml
<declare-styleable name="custom_view">
    <attr name="label" format="string|reference"/>
</declare-styleable>
```

You can set the string value of this attribute to a TextView the following way:

```java
TypedValue typedValue = new TypedValue();
getTheme().resolveAttribute(R.attr.label, typedValue, true);
textView.setText(typedValue.resourceId);
// textView.setText(typedValue.string); // DON'T DO THAT!!!
```

or the following way:

```java
TypedArray typedArray  = getTheme().obtainStyledAttributes(set, R.styleable.custom_view, defStyleAttr, defStyleRes);
textView.setText(typedArray.getResourceId(R.styleable.custom_view_label, -1)); // Get the resource id of the stylable attribute under the current theme
// textView.setText(typedArray.getString(R.styleable.custom_view_label, -1)); // DON'T DO THAT!!!
typedArray.recycle();
```

Note that if you try to resolve the string value directly from the theme methods, the call will not pass through the SDK. The trick here is to resolve the resource id from the theme methods.


### TxNative and 3rd party libraries

Some libs may contain their own localized strings, views or activities. In such as case, you don't want TxNative to interfere with string loading. To accomplish that, make sure that you pass a non-wrapped context to the library's initialization method:

```java
SomeSDK.init(getApplicationContext());
```

Note however that if a `View` provided by the library is used inside your app's activity, `TxNative` will be used during that view's inflation (if your activity is set up correctly). In that case, any library strings will not be found in TxNative translations and the result will depend on the missing policy used. `SourceStringPolicy` will return the source string provided by the library, which will probably be in English. Using, `AndroidMissingPolicy` will return the localized string using the library's localized string resources, as expected.

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
1. Use TxNative as a dependency in the lib.
2. Implement TxNative in the lib's activities.
3. Note that TxNative should not be initialized inside the lib. The main app is responsible for this.

## Limitations

The SDK has some limitations, which most of the time can be overcome with workarouds.

### String Arrrays

Currently, the SDK does not support [String arrays](https://developer.android.com/guide/topics/resources/string-resource#StringArray). The command line tool will not upload them to Transifex and the SDK will not override the respective methods. String arrays presentation will work as normal using Android's localization system, which will require that you have them translated in the respective `strings.xml` files.

### Menu XML files

Strings that are referenced in [menu](https://developer.android.com/guide/topics/ui/menus) layout files will not be handled by the SDK. They will use Android's localization system as normal.

### ActionBar

Even though the SDK handles the strings referenced in a [`Toolbar`](https://developer.android.com/reference/androidx/appcompat/widget/Toolbar), it won't handle strings referenced in an [`ActionBar`](https://developer.android.com/reference/android/app/ActionBar). You will have to set them programmatically (e.g. by calling [`setTitle()`](https://developer.android.com/reference/android/app/ActionBar#setTitle(java.lang.CharSequence))) to take advantage of the SDK. Otherwise, Android's localization system will be used.

## Video resources

* [How to install the Transifex Native Android SDK](https://youtu.be/1Z3eTtzI1IA)
* [How to push strings with Transifex Native in Android](https://youtu.be/V0L1cjaQTGk)
* [How to bundle translations with Transifex Native in your Android package](https://youtu.be/zNPh4bIYOxY)


## License
Licensed under Apache License 2.0, see [LICENSE](LICENSE) file.
