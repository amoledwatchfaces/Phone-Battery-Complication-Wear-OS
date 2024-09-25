import java.io.FileInputStream
import java.util.Properties

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("org.jetbrains.kotlin.plugin.compose")
    id ("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")
    id ("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
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

    compileSdk = 35

    defaultConfig {
        applicationId = "com.weartools.phonebattcomp"
        minSdk = 26
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "17"
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
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // Review
    implementation ("com.google.android.play:review-ktx:2.0.1")
    implementation ("com.google.android.play:review:2.0.1")

    // Wearable
    implementation ("com.google.android.gms:play-services-wearable:18.2.0")
    implementation ("androidx.wear:wear-remote-interactions:1.0.0")

    // Splash Screen
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // Icons
    implementation ("androidx.compose.material:material-icons-extended:1.7.2")

    // Compose
    implementation ("androidx.activity:activity-compose:1.9.2")
    implementation ("androidx.compose.material3:material3:1.3.0")
    implementation ("androidx.compose.material3:material3-window-size-class:1.3.0")
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation ("androidx.compose.material3:material3")
    implementation ("androidx.compose.material:material:1.7.2")

    // Navigation
    implementation ("androidx.navigation:navigation-compose:2.8.1")
    implementation ("androidx.activity:activity-ktx:1.9.2")
    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // Used for Datastore
    implementation ("androidx.datastore:datastore-preferences:1.1.1")

    // Permissions
    implementation ("com.google.accompanist:accompanist-permissions:0.34.0")

    // Debug
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.02"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Hilt
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.google.dagger:hilt-android:2.51")
    ksp ("com.google.dagger:hilt-compiler:2.51")

    // Firebase
    implementation (platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation ("com.google.firebase:firebase-crashlytics")
    implementation ("com.google.firebase:firebase-analytics")
}