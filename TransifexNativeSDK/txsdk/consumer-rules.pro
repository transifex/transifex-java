# Keep LocaleData inner classes as is, so that Gson works as expected
-keep class com.transifex.common.LocaleData { *; }
-keep class com.transifex.common.LocaleData$* { *; }

# Keep class names for better log ouput
-keepnames class com.transifex.txnative.**
-keepnames class com.transifex.common.**