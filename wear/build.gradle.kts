plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-parcelize")
}

@Suppress("UnstableApiUsage") //TODO: CHECK LATER
android {

    compileSdk = 33

    defaultConfig {
        applicationId = "com.weartools.phonebattcomp"
        minSdk = 27
        targetSdk = 33
        versionCode = 10000245
        versionName = "2.4.5"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    namespace = "com.weartools.phonebattcomp"
}

dependencies {
    val composeUiVersion = rootProject.extra["compose_version"]
    val composeWearVersion = rootProject.extra["compose_wear_version"]

    // WEAR OS
    implementation ("com.google.android.gms:play-services-wearable:18.0.0")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.1.1")
    implementation ("androidx.wear:wear-remote-interactions:1.0.0")
    compileOnly ("com.google.android.wearable:wearable:2.9.0")

    // COMPOSE
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.compose.ui:ui:$composeUiVersion")
    implementation ("androidx.wear.compose:compose-material:$composeWearVersion")
    implementation ("androidx.wear.compose:compose-foundation:$composeWearVersion")
    implementation ("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation ("androidx.compose.material:material-icons-extended:$composeUiVersion")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-tooling:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:$composeUiVersion")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Used for Datastore
    implementation ("androidx.datastore:datastore-preferences:1.0.0")

    // SPLASH SCREEN
    implementation ("androidx.core:core-splashscreen:1.0.1")
}