package sk.ainet.kotlinconf.models.cnn

import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

/** Anchor whose classloader carries this module's bundled java-resources. */
private object ModelResourceAnchor

internal actual fun openModelResource(path: String): Source {
    val stream = ModelResourceAnchor::class.java.classLoader
        ?.getResourceAsStream(path)              // classLoader paths have no leading slash
        ?: error("model resource '$path' not found on the classpath")
    return stream.asSource().buffered()
}
