import java.io.FileInputStream
import java.util.Properties

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-parcelize")
    id ("kotlinx-serialization")
    id ("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {

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

    compileSdk = 34

    defaultConfig {
        applicationId = "com.weartools.phonebattcomp"
        minSdk = 27
        targetSdk = 34
        versionCode = 10000459
        versionName = "4.5.9"

        buildConfigField("String", "CAPABILITY_MOBILE_APP", "\"phonebattcomp_mobile_app\"")
        buildConfigField("String", "PLAY_STORE_APP_URI", "\"market://details?id=com.weartools.phonebattcomp\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.weartools.phonebattcomp"
}

dependencies {
    val composeUiVersion = "1.7.2"
    val composeWearVersion = "1.4.0"

    // WEAR OS
    implementation ("com.google.android.gms:play-services-wearable:18.2.0")
    implementation ("androidx.wear.watchface:watchface:1.2.1")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0-alpha04")
    implementation ("androidx.wear:wear-remote-interactions:1.0.0")
    compileOnly ("com.google.android.wearable:wearable:2.9.0")

    // COMPOSE
    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.compose.ui:ui:$composeUiVersion")
    implementation ("androidx.wear.compose:compose-material:$composeWearVersion")
    implementation ("androidx.wear.compose:compose-foundation:$composeWearVersion")
    implementation ("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
    implementation ("androidx.activity:activity-compose:1.9.2")
    implementation ("androidx.compose.material:material-icons-extended:$composeUiVersion")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-tooling:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:$composeUiVersion")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Used for Datastore
    implementation ("androidx.datastore:datastore-preferences:1.1.1")

    // Serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // SPLASH SCREEN
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // Used for WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.9.1")

    // TILE
    implementation("androidx.wear.protolayout:protolayout-expression:1.2.0")
    implementation("androidx.wear.protolayout:protolayout:1.2.0")
    implementation("androidx.wear.protolayout:protolayout-material:1.2.0")
    implementation("androidx.wear.tiles:tiles:1.4.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    // HILT
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.google.dagger:hilt-android:2.51")
    ksp ("com.google.dagger:hilt-compiler:2.51")
}