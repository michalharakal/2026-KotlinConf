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
            // GGUF reader for the pretrained mnist_cnn.gguf weights.
            implementation(libs.skainet.io.core)
            implementation(libs.skainet.io.gguf)
            implementation(libs.kotlinx.io.core)
        }
        // The ONE physical mnist_cnn.gguf is packaged as a java-resource into the JVM jar…
        jvmMain.configure { resources.srcDir("src/weights") }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        // …and onto the JVM test classpath, so the cross-platform test can read it.
        jvmTest.configure { resources.srcDir("src/weights") }
    }
}

android {
    namespace = "sk.ainet.kotlinconf.models.cnn"
    // …and the SAME physical file into the AAR as a java-resource (NOT an asset), so an app
    // consuming this module reads it via the classloader, identically to the JVM.
    sourceSets.getByName("main").resources.srcDir("src/weights")
}
