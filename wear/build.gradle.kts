
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

    signingConfigs {
        create("release") {
            val keystoreFileEnv = System.getenv("KEYSTORE_FILE")
            val keystorePasswordEnv = System.getenv("KEYSTORE_PASSWORD")
            val keyAliasEnv = System.getenv("KEY_ALIAS")
            val keyPasswordEnv = System.getenv("KEY_PASSWORD")

            if (!keystoreFileEnv.isNullOrEmpty() && !keystorePasswordEnv.isNullOrEmpty() && !keyAliasEnv.isNullOrEmpty() && !keyPasswordEnv.isNullOrEmpty()) {
                storeFile = file(keystoreFileEnv)
                storePassword = keystorePasswordEnv
                keyAlias = keyAliasEnv
                keyPassword = keyPasswordEnv
            } else {
                val localPropertiesFile = file("C:\\Users\\amoledwatchfaces\\WatchFaceStudio\\keystore\\keystore.properties")
                val localKeystoreFile = file("C:\\Users\\amoledwatchfaces\\WatchFaceStudio\\keystore\\keystore.jks")
                if (localPropertiesFile.exists() && localKeystoreFile.exists()) {
                    val keystoreProperties = Properties().apply {
                        load(FileInputStream(localPropertiesFile))
                    }
                    storeFile = localKeystoreFile
                    keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
                    storePassword = keystoreProperties.getProperty("STORE_PASSWORD")
                    keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
                } else {
                    val debugConfig = signingConfigs.getByName("debug")
                    storeFile = debugConfig.storeFile
                    storePassword = debugConfig.storePassword
                    keyAlias = debugConfig.keyAlias
                    keyPassword = debugConfig.keyPassword
                }
            }
        }
    }

    compileSdk = 37

    defaultConfig {
        applicationId = "com.weartools.phonebattcomp"
        minSdk = 29
        targetSdk = 37
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
    val composeBom = platform ("androidx.compose:compose-bom:2026.03.00")

    // Wear OS
    implementation ("com.google.android.gms:play-services-wearable:20.0.1")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0")
    implementation ("androidx.wear:wear-remote-interactions:1.2.0")
    compileOnly ("com.google.android.wearable:wearable:2.9.0")

    // General compose dependencies
    implementation (composeBom)
    implementation ("androidx.activity:activity-compose:1.13.0")
    implementation ("androidx.compose.ui:ui:1.12.0")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.12.0")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")
    implementation ("androidx.compose.animation:animation-graphics-android:1.12.0")

    // Compose for Wear OS dependencies
    implementation ("androidx.wear.compose:compose-material3:1.6.2")
    implementation ("androidx.wear.compose:compose-navigation:1.6.2")

    // Foundation is additive, so you can use the mobile version in your Wear OS app.
    implementation ("androidx.wear.compose:compose-foundation:1.6.2")

    // Wear OS preview annotations
    implementation ("androidx.wear.compose:compose-ui-tooling:1.6.2")

    // Testing
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.3.0")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.12.0")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.12.0")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:1.12.0")

    // Core
    implementation ("androidx.core:core-ktx:1.19.0")

    // Lifecycle
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.11.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    // DataStore
    implementation ("androidx.datastore:datastore:1.2.1")

    // Serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // Splash Screen
    implementation ("androidx.core:core-splashscreen:1.2.0")

    // WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.11.2")

    // Tile
    implementation ("androidx.wear.protolayout:protolayout-expression:1.4.2")
    implementation ("androidx.wear.protolayout:protolayout:1.4.2")
    implementation ("androidx.wear.protolayout:protolayout-material3:1.4.2")
    implementation ("androidx.wear.tiles:tiles:1.6.2")

    // ListenableFuture
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.11.0")

    // Core Wear Widget and Remote Compose libraries
    implementation ("androidx.compose.remote:remote-creation-compose:1.0.0-alpha17")
    implementation ("androidx.compose.remote:remote-core:1.0.0-alpha17")
    implementation ("androidx.glance.wear:wear:1.0.0-alpha16")
    implementation ("androidx.glance.wear:wear-core:1.0.0-alpha16")
    implementation ("androidx.wear.compose.remote:remote-material3:1.0.0-alpha09")

    // Tooling for previews (optional, but recommended)
    implementation ("androidx.compose.remote:remote-tooling-preview:1.0.0-alpha17")
    implementation ("androidx.wear.tiles:tiles-tooling-preview:1.6.2")
    debugImplementation ("androidx.wear.tiles:tiles-renderer:1.6.2")

    // Hilt
    implementation ("androidx.hilt:hilt-navigation-compose:1.4.0")
    implementation ("com.google.dagger:hilt-android:2.60.1")
    ksp ("com.google.dagger:hilt-compiler:2.60.1")

    // Firebase
    implementation (platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation ("com.google.firebase:firebase-crashlytics")
}