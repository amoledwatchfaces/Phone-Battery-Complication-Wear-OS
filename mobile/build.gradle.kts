
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.plugin.compose")
    id ("kotlinx-serialization")
    id ("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")
    id ("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
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

    compileSdk = 37

    defaultConfig {
        applicationId = "com.weartools.phonebattcomp"
        minSdk = 26
        targetSdk = 37
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String

        buildConfigField("String", "CAPABILITY_WEAR_APP", "\"phonebattcomp_wear_app\"")
        buildConfigField("String", "PLAY_STORE_APP_URI", "\"market://details?id=$applicationId\"")
        versionNameSuffix = "-mobile"
        versionCode = 10000 + (versionCode ?: 0)
    }

    testBuildType = "debug"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    namespace = "com.weartools.phonebattcomp"
}

dependencies {

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.11.0")

    // Review
    implementation ("com.google.android.play:review-ktx:2.0.2")
    implementation ("com.google.android.play:review:2.0.2")

    // Wearable
    implementation ("com.google.android.gms:play-services-wearable:20.0.1")
    implementation ("androidx.wear:wear-remote-interactions:1.2.0")

    // Splash Screen
    implementation ("androidx.core:core-splashscreen:1.2.0")

    // Compose
    implementation (platform("androidx.compose:compose-bom:2026.08.00"))
    implementation ("androidx.activity:activity-compose")
    implementation ("androidx.navigation:navigation-compose")
    implementation ("androidx.compose.material3:material3")
    implementation ("androidx.compose.material3:material3-window-size-class")
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.ui:ui-graphics")
    implementation ("androidx.compose.ui:ui-tooling-preview")
    implementation ("androidx.compose.material:material")
    implementation ("androidx.compose.material:material-icons-extended")


    implementation ("androidx.activity:activity-ktx:1.13.0")
    implementation ("androidx.core:core-ktx:1.19.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")

    // Serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // Used for Datastore
    implementation ("androidx.datastore:datastore:1.2.1")

    // Permissions
    implementation ("com.google.accompanist:accompanist-permissions:0.37.3")

    // Debug
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Hilt
    implementation ("androidx.hilt:hilt-navigation-compose:1.4.0")
    implementation ("com.google.dagger:hilt-android:2.60.1")
    ksp ("com.google.dagger:hilt-compiler:2.60.1")

    // Firebase
    implementation (platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation ("com.google.firebase:firebase-crashlytics")
}