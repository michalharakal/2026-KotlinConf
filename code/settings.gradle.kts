rootProject.name = "kotlinconf-skainet"

pluginManagement {
    // Convention plugins for the standalone model modules live in an included build.
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        // Everything resolves from Maven Central (SKaiNET 0.36.0 is published there).
        // No mavenLocal: it can shadow Central's variant-aware metadata and break KMP targets.
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":shared")
include(":cli")
include(":androidApp")
include(":webApp")

// Standalone, publishable model modules (group sk.ainet.kotlinconf.models).
include(":models:model-common")
include(":models:mnist-cnn")
include(":models:sinus-mlp")
include(":models:tiny-transformer")
