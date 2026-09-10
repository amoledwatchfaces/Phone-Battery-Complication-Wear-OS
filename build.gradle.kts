import java.nio.file.Files

plugins {
    id ("com.android.application") version ("9.4.0") apply false
    id ("org.jetbrains.kotlin.android") version ("2.4.10") apply false
    id ("org.jetbrains.kotlin.plugin.compose") version ("2.4.10") apply false
    id ("org.jetbrains.kotlin.plugin.parcelize") version ("2.3.10") apply false
    id ("com.google.dagger.hilt.android") version ("2.60.1") apply false
    id ("com.google.devtools.ksp") version ("2.3.4") apply false
    id ("com.google.gms.google-services") version ("4.5.0") apply false
    id ("com.google.firebase.crashlytics") version ("3.0.7") apply false
}

buildscript {

    /** Set version for wear & mobile modules **/
    extra.set("versionCode", 10000600)
    extra.set("versionName", "6.0.0")

    dependencies {
        classpath ("com.android.tools.build:gradle:9.4.0")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:2.4.10")
    }
    repositories {
        google()
    }
}

tasks.register("clean", Delete::class) {
    description = "Clean build directory across all modules and strip read-only attributes"
    val allBuildDirs = listOf(rootProject.layout.buildDirectory.get().asFile) + subprojects.map { it.layout.buildDirectory.get().asFile }
    delete(allBuildDirs)
    doFirst {
        val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
        if (isWindows) {
            allBuildDirs.forEach { dir ->
                if (dir.exists()) {
                    dir.walkBottomUp().forEach { file ->
                        try {
                            Files.setAttribute(file.toPath(), "dos:readonly", false)
                        } catch (_: Exception) {
                            // File might be already gone or locked
                        }
                    }
                }
            }
        }
    }
}