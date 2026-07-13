plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvmToolchain(21)

    jvm()
    androidTarget()

    sourceSets {
        commonMain.dependencies {
            // `api` so downstream modules (:cli, :androidApp) get the SKaiNET DSL types.
            api(project.dependencies.platform(libs.skainet.bom))
            api(libs.skainet.lang.core)
            api(libs.skainet.backend.cpu)
            api(libs.skainet.lang.models)
            // Graph execution + gradient tape (Stage 5 training) live here.
            api(libs.skainet.compile.core)
            api(libs.skainet.compile.dag)
            // GGUF reader — Stage 4 loads the pretrained mnist_cnn.gguf weights.
            api(libs.skainet.io.core)
            api(libs.skainet.io.gguf)

            api(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.io.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "sk.ainet.kotlinconf.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
