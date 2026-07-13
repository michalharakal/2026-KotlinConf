plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":shared"))
}

application {
    // Overridden per stage via `-PmainClass=...` or the run* tasks below.
    mainClass.set("sk.ainet.kotlinconf.cli.MainKt")
}

// One convenience run task per talk stage.
listOf(
    "1" to "sk.ainet.kotlinconf.cli.Stage1Kt",
    "2" to "sk.ainet.kotlinconf.cli.Stage2Kt",
    "3" to "sk.ainet.kotlinconf.cli.Stage3Kt",
    "4" to "sk.ainet.kotlinconf.cli.Stage4Kt",
    "5" to "sk.ainet.kotlinconf.cli.Stage5Kt",
).forEach { (n, cls) ->
    tasks.register<JavaExec>("runStage$n") {
        group = "kotlinconf"
        description = "Run talk Stage $n"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(cls)
    }
}
