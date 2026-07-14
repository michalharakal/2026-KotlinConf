plugins {
    `kotlin-dsl`
}

// The precompiled convention plugin in src/main/kotlin applies the Kotlin Multiplatform and
// Android Library plugins by id, so their implementations must be on this build's classpath.
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
}
