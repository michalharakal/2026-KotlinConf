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
            // Umbrella: re-export the standalone model modules so :cli and :androidApp get the
            // CNN / MLP / transformer (and their async loaders) transitively via :shared.
            api(project(":models:model-common"))
            api(project(":models:mnist-cnn"))
            api(project(":models:sinus-mlp"))
            api(project(":models:tiny-transformer"))

            // The foundational `tensors` + `linear` demos live here and need only the DSL.
            // `api` so downstream modules get the SKaiNET DSL types.
            api(project.dependencies.platform(libs.skainet.bom))
            api(libs.skainet.lang.core)
            api(libs.skainet.backend.cpu)

            api(libs.kotlinx.coroutines)
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
