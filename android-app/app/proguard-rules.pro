# Keep androidx.webkit
-keep class androidx.webkit.** { *; }

# Methods called from the game's JavaScript must survive minification.
-keepattributes JavascriptInterface
-keepclassmembers class com.nivrutti.blockpuzzlejewel.MainActivity$GameBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Google Mobile Ads / UMP ship their own consumer rules; nothing extra needed.
