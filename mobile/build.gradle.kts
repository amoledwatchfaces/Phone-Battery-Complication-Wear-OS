import java.io.FileInputStream
import java.util.Properties

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("org.jetbrains.kotlin.plugin.compose")
    id ("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")
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
        versionCode = 10000399
        versionName = "3.9.8"

        buildConfigField("String", "CAPABILITY_WEAR_APP", "\"phonebattcomp_wear_app\"")
        buildConfigField("String", "PLAY_STORE_APP_URI", "\"market://details?id=com.weartools.phonebattcomp\"")
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

    // PREVIOUS APP
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    implementation ("com.google.android.gms:play-services-wearable:18.2.0")

    implementation ("com.google.android.play:review-ktx:2.0.1")
    implementation ("com.google.android.play:review:2.0.1")
    implementation ("androidx.wear:wear-remote-interactions:1.0.0")

    // SPLASH SCREEN
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // ICONS
    implementation ("androidx.compose.material:material-icons-extended:1.6.8")

    // COMPOSE
    implementation ("androidx.activity:activity-compose:1.9.0")

    implementation ("androidx.compose.material3:material3:1.2.1")
    implementation ("androidx.compose.material3:material3-window-size-class:1.2.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation ("androidx.compose.material3:material3")
    implementation ("androidx.compose.material:material:1.6.8")

    // NAVIGATION
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation ("androidx.activity:activity-ktx:1.9.0")
    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")

    // Used for Datastore
    implementation ("androidx.datastore:datastore-preferences:1.1.1")

    // DEBUG
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // HILT
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.google.dagger:hilt-android:2.51")
    ksp ("com.google.dagger:hilt-compiler:2.51")
}