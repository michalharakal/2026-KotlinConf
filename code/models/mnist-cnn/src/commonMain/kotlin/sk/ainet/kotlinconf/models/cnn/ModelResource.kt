package sk.ainet.kotlinconf.models.cnn

import kotlinx.io.Source
import kotlinx.io.readByteArray

/**
 * Opens a model resource bundled in this module (a classpath path such as `"mnist_cnn.gguf"`)
 * as a kotlinx-io [Source].
 *
 * The weight file ships once, in `src/weights/`, and is packaged as a java-resource into both
 * the JVM jar and the Android AAR. Reading it needs a platform classloader, which is why this
 * is `expect`/`actual`: `kotlinx.io`'s `InputStream.asSource()` bridge is JVM-only, so it
 * cannot live in `commonMain`.
 */
internal expect fun openModelResource(path: String): Source

/** Reads a bundled model resource fully into a [ByteArray]. */
internal fun readModelResourceBytes(path: String): ByteArray =
    openModelResource(path).use { it.readByteArray() }
