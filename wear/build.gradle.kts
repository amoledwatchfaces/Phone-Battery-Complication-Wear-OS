
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    id ("com.android.application")
    id ("kotlinx-serialization")
    id ("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")
    id ("org.jetbrains.kotlin.plugin.compose")
    id ("org.jetbrains.kotlin.plugin.parcelize")
    id ("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    val appVersionCode: Int by rootProject.extra
    val appVersionName: String by rootProject.extra

    signingConfigs {
        create("release") {
            val keystoreProperties = Properties().apply{
                load(FileInputStream(file("C:\\Users\\amoledwatchfaces\\WatchFaceStudio\\keystore\\keystore.properties")))
            }
            storeFile = file("C:\\Users\\amoledwatchfaces\\WatchFaceStudio\\keystore\\keystore.jks")
            keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
            storePassword = keystoreProperties.getProperty("STORE_PASSWORD")
            keyPassword= keystoreProperties.getProperty("KEY_PASSWORD")
        }
    }

    compileSdk = 36

    defaultConfig {
        applicationId = "com.weartools.phonebattcomp"
        minSdk = 27
        targetSdk = 36
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String

        buildConfigField("String", "CAPABILITY_MOBILE_APP", "\"phonebattcomp_mobile_app\"")
        buildConfigField("String", "PLAY_STORE_APP_URI", "\"market://details?id=$applicationId\"")
        versionNameSuffix = "-wear"
        versionCode = 20000 + (versionCode ?: 0)
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    namespace = "com.weartools.phonebattcomp"
}

dependencies {
    val composeBom = platform ("androidx.compose:compose-bom:2025.08.00")

    // Wear OS
    implementation ("com.google.android.gms:play-services-wearable:19.0.0")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0-rc01")
    implementation ("androidx.wear:wear-remote-interactions:1.1.0")
    compileOnly ("com.google.android.wearable:wearable:2.9.0")

    // General compose dependencies
    implementation (composeBom)
    implementation ("androidx.activity:activity-compose:1.12.4")
    implementation ("androidx.compose.ui:ui:1.10.3")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.10.3")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")

    // Compose for Wear OS dependencies
    implementation ("androidx.wear.compose:compose-material3:1.5.6")
    implementation ("androidx.wear.compose:compose-navigation:1.5.6")

    // Foundation is additive, so you can use the mobile version in your Wear OS app.
    implementation ("androidx.wear.compose:compose-foundation:1.5.6")

    // Wear OS preview annotations
    implementation ("androidx.wear.compose:compose-ui-tooling:1.5.6")

    // Testing
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.3.0")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.10.3")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.10.3")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:1.10.3")

    // Core
    implementation ("androidx.core:core-ktx:1.17.0")

    // Lifecycle
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // DataStore
    implementation ("androidx.datastore:datastore:1.2.0")

    // Serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    // Splash Screen
    implementation ("androidx.core:core-splashscreen:1.2.0")

    // WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.11.1")

    // Tile
    implementation ("androidx.wear.protolayout:protolayout-expression:1.3.0")
    implementation ("androidx.wear.protolayout:protolayout:1.3.0")
    implementation ("androidx.wear.protolayout:protolayout-material:1.3.0")
    implementation ("androidx.wear.tiles:tiles:1.5.0")

    // ListenableFuture
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.10.2")

    // Hilt
    implementation ("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation ("com.google.dagger:hilt-android:2.59.1")
    ksp ("com.google.dagger:hilt-compiler:2.59.1")

    // Firebase
    implementation (platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation ("com.google.firebase:firebase-crashlytics")
}