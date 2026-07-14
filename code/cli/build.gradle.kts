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
    // Overridden per pattern via the run* tasks below.
    mainClass.set("sk.ainet.kotlinconf.cli.CnnKt")
}

// One convenience run task per pattern the talk walks through.
listOf(
    "Tensors" to "sk.ainet.kotlinconf.cli.TensorsKt",
    "Linear" to "sk.ainet.kotlinconf.cli.LinearKt",
    "Mlp" to "sk.ainet.kotlinconf.cli.MlpKt",
    "Cnn" to "sk.ainet.kotlinconf.cli.CnnKt",
    "Transformer" to "sk.ainet.kotlinconf.cli.TransformerKt",
).forEach { (name, cls) ->
    tasks.register<JavaExec>("run$name") {
        group = "kotlinconf"
        description = "Run the $name demo"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(cls)
    }
}
