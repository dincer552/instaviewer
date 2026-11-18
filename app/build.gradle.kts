plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
val appVersionCode = providers.gradleProperty("appVersionCode").orNull?.toIntOrNull() ?: 1
val appVersionName = providers.gradleProperty("appVersionName").orNull ?: "1.0.0"
val signingStorePath = System.getenv("ANDROID_KEYSTORE_PATH")

android {
    namespace = "com.dincer552.instaviewer"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.dincer552.instaviewer"
        minSdk = 24
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }
    signingConfigs {
        create("releaseKey") {
            if (!signingStorePath.isNullOrBlank()) {
                storeFile = file(signingStorePath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = "hattrickssh"
                keyPassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (!signingStorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("releaseKey")
            }
        }
    }
}
