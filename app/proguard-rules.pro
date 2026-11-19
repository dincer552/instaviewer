# ProGuard rules for InstaViewer

# Keep the main activity
-keep class com.dincer552.instaviewer.MainActivity { *; }

# Keep WebView JavaScript interfaces if any
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep names for reflection
-keepnames class * implements java.io.Serializable
-keepnames class * implements android.os.Parcelable

# Don't warn about WebView internals
-dontwarn android.webkit.**

# Optimize
-optimizationpasses 5
-allowaccessmodification

# Keep source file and line number for debugging (release builds)
-keepattributes SourceFile,LineNumberTable

# Remove logging in release (optional, kept for debugging WebView)
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
# }