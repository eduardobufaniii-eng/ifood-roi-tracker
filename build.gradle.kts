buildscript {
    ext {
        compose_bom_version = "2024.10.00"
        hilt_version = "2.51.1"
        room_version = "2.6.1"
        coroutines_version = "1.8.0"
        lifecycle_version = "2.8.0"
        navigation_version = "2.8.0"
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51.1")
    }
}

plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("kotlin-kapt") version "1.9.24" apply false
}