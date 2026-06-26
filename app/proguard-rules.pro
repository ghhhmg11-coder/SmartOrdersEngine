-keep class com.smartorders.engine.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
