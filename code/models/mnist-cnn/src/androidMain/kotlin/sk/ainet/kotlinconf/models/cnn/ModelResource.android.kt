package sk.ainet.kotlinconf.models.cnn

import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

/** Anchor whose classloader carries this module's bundled java-resources (packaged into the AAR). */
private object ModelResourceAnchor

// Identical to the JVM actual: on Android the weight file is an AAR java-resource (not an asset),
// read through the classloader — so no Android Context/AssetManager is needed.
internal actual fun openModelResource(path: String): Source {
    val stream = ModelResourceAnchor::class.java.classLoader
        ?.getResourceAsStream(path)
        ?: error("model resource '$path' not found on the classpath")
    return stream.asSource().buffered()
}
