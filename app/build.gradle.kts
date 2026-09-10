plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "me.ikirby.pixelutils"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.ikirby.pixelutils"
        minSdk = 34
        targetSdk = 36
        versionCode = 13
        versionName = "13"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        aidl = true
        viewBinding = true
        buildConfig = true
    }
}

base {
    archivesName = "PixelCarrierSettings"
}

dependencies {
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.hiddenapibypass)
}