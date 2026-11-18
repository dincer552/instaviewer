plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.dincer552.instaviewer"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.dincer552.instaviewer"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}
