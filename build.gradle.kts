// Root build file. Plugin versions are declared here (with `apply false`) and
// applied per-module in app/build.gradle.kts, following the standard
// Android Gradle Plugin version-catalog-free convention.
plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
