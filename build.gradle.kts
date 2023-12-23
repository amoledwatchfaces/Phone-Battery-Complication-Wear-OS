plugins {
    val kotlinVersion = "1.9.10"
    id("com.android.application") version("8.2.0") apply false
    id("org.jetbrains.kotlin.android") version(kotlinVersion) apply false
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}