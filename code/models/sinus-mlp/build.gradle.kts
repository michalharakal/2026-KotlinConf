plugins {
    id("kotlinconf.model-module")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":models:model-common"))
            api(project.dependencies.platform(libs.skainet.bom))
            api(libs.skainet.lang.core)
            api(libs.skainet.backend.cpu)
            // Pretrained sin(x) weights ship inside skainet-lang-models (SinusApproximatorWandB).
            implementation(libs.skainet.lang.models)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "sk.ainet.kotlinconf.models.mlp"
}
