import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Small Compose UI kit vendored from the official SKaiNET examples
// (github.com/SKaiNET-developers/SKaiNET-examples · skainet-ui) so it compiles against this
// repo's Kotlin 2.4.0 / Compose 1.11.1 toolchain: the rotating-logo loader and the plot API.
kotlin {
    jvmToolchain(21)

    androidTarget()
    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}

android {
    namespace = "sk.ainet.kotlinconf.uikit"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
