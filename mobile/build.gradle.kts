plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
}

android {

    compileSdk = 34

    defaultConfig {
        applicationId = "com.weartools.phonebattcomp"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 33
        versionCode = 10000302
        versionName = "3.0.0"
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    namespace = "com.weartools.phonebattcomp"
}

dependencies {

    // PREVIOUS APP
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation ("com.google.android.gms:play-services-wearable:18.1.0")

    implementation ("com.google.android.play:review-ktx:2.0.1")
    implementation ("com.google.android.play:review:2.0.1")
    implementation ("androidx.wear:wear-remote-interactions:1.0.0")

    // SPLASH SCREEN
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // ICONS
    implementation ("androidx.compose.material:material-icons-extended:1.5.4")

    // COMPOSE
    implementation ("androidx.activity:activity-compose:1.8.0")

    implementation ("androidx.compose.material3:material3:1.2.0-alpha10")
    implementation ("androidx.compose.material3:material3-window-size-class:1.2.0-alpha10")

    implementation(platform("androidx.compose:compose-bom:2023.05.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation ("androidx.compose.material3:material3")
    implementation ("androidx.compose.material:material:1.5.4")

    // NAVIGATION
    implementation("androidx.navigation:navigation-compose:2.7.4")

    implementation ("androidx.activity:activity-ktx:1.8.0")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // DEBUG
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}