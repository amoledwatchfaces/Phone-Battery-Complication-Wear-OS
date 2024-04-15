plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-parcelize")
}

android {

    compileSdk = 34

    defaultConfig {
        applicationId = "com.weartools.phonebattcomp"
        minSdk = 27
        targetSdk = 33
        versionCode = 10000332
        versionName = "3.3.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    namespace = "com.weartools.phonebattcomp"
}

dependencies {
    val composeUiVersion = "1.6.5"
    val composeWearVersion = "1.3.1"

    // WEAR OS
    implementation ("com.google.android.gms:play-services-wearable:18.1.0")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0-alpha02")
    implementation ("androidx.wear:wear-remote-interactions:1.0.0")
    compileOnly ("com.google.android.wearable:wearable:2.9.0")

    // COMPOSE
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.compose.ui:ui:$composeUiVersion")
    implementation ("androidx.wear.compose:compose-material:$composeWearVersion")
    implementation ("androidx.wear.compose:compose-foundation:$composeWearVersion")
    implementation ("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.compose.material:material-icons-extended:$composeUiVersion")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-tooling:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:$composeUiVersion")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Used for Datastore
    implementation ("androidx.datastore:datastore-preferences:1.0.0")

    // SPLASH SCREEN
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // TILE
    implementation("androidx.wear.protolayout:protolayout-expression:1.1.0")
    implementation("androidx.wear.protolayout:protolayout:1.1.0")
    implementation("androidx.wear.protolayout:protolayout-material:1.1.0")
    implementation("androidx.wear.tiles:tiles:1.3.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    // HOROLOGIST
    implementation ("com.google.android.horologist:horologist-annotations:0.6.3")
}