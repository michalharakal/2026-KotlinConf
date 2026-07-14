import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("kotlinconf.model-module")
}

kotlin {
    // The decoder-only transformer trains live with no weight file, so it also compiles for
    // the browser — the web demo consumes exactly this module.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(project(":models:model-common"))
            api(project.dependencies.platform(libs.skainet.bom))
            api(libs.skainet.lang.core)
            api(libs.skainet.backend.cpu)
            // Graph execution + gradient tape for live training.
            implementation(libs.skainet.compile.core)
            implementation(libs.skainet.compile.dag)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "sk.ainet.kotlinconf.models.transformer"
}
