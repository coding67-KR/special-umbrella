plugins {
    id("com.android.application")
}

android {
    namespace = "com.coding67.kkeutmal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.coding67.kkeutmal"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
