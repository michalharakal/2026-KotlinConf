// Convention plugin shared by every standalone SKaiNET model module:
// a jvm + android KMP library that is publishable to Maven (local or remote).
//
// Modules that also need the browser target (e.g. tiny-transformer, model-common) add
// `wasmJs { browser() }` on top of this in their own build script.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("maven-publish")
}

group = "sk.ainet.kotlinconf.models"
version = "0.1.0"

kotlin {
    jvmToolchain(21)

    jvm()
    androidTarget {
        // Publish the release variant so consumers resolving the `android` publication get an AAR.
        publishLibraryVariants("release")
    }
}

android {
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Required for AGP + maven-publish so the KMP android publication has a single variant.
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}
