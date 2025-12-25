// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
// build.gradle.kts (Project-level)
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.4")
        classpath("com.google.gms:google-services:4.4.0")
    }

    repositories {
        google()
        mavenCentral()
    }
}
