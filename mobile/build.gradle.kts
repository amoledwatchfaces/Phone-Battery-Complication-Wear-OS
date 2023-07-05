plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
}

@Suppress("UnstableApiUsage") //TODO: CHECK LATER
android {

    compileSdk = 33

    defaultConfig {
        applicationId = "com.weartools.phonebattcomp"
        minSdk = 23
        targetSdk = 33
        versionCode = 10000244
        versionName = "2.4.5"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    namespace = "com.weartools.phonebattcomp"
}

dependencies {
    implementation ("com.google.android.gms:play-services-wearable:18.0.0")

    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")

    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.preference:preference:1.2.0")
    implementation ("com.google.android.material:material:1.9.0")

    implementation ("androidx.wear:wear-remote-interactions:1.0.0")
    implementation ("androidx.activity:activity-ktx:1.7.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // SPLASH SCREEN
    implementation ("androidx.core:core-splashscreen:1.0.1")
}