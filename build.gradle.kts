plugins {
    id("com.android.application") version("8.2.2") apply false
    id("org.jetbrains.kotlin.android") version("1.9.22") apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

buildscript {
    dependencies {
        classpath ("com.android.tools.build:gradle:8.3.2")
    }
    repositories {
        google()
    }
}