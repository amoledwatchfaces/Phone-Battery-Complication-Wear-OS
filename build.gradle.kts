plugins {
    id ("com.android.application") version ("9.3.2") apply false
    id ("org.jetbrains.kotlin.android") version ("2.4.0") apply false
    id ("org.jetbrains.kotlin.plugin.compose") version ("2.4.0") apply false
    id ("org.jetbrains.kotlin.plugin.parcelize") version ("2.3.10") apply false
    id ("com.google.dagger.hilt.android") version ("2.60.1") apply false
    id ("com.google.devtools.ksp") version ("2.3.4") apply false
    id ("com.google.gms.google-services") version ("4.5.0") apply false
    id ("com.google.firebase.crashlytics") version ("3.0.7") apply false
}

tasks.register("clean", Delete::class) {
    description = "Clean build directory"
    delete(rootProject.layout.buildDirectory)
}

buildscript {

    /** Set version for wear & mobile modules **/
    extra.set("versionCode", 10000587)
    extra.set("versionName", "5.8.7")

    dependencies {
        classpath ("com.android.tools.build:gradle:9.3.2")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:2.4.0")
    }
    repositories {
        google()
    }
}