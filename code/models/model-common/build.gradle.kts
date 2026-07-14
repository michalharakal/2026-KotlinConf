import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("kotlinconf.model-module")
}

kotlin {
    // model-common carries only the pure-common load-state types, so it also compiles for the
    // browser and can be shared by the web demo's transformer module.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            // Exposed so downstream model modules get Flow/coroutine types transitively.
            api(libs.kotlinx.coroutines)
        }
    }
}

android {
    namespace = "sk.ainet.kotlinconf.models.common"
}
