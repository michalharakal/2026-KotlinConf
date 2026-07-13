package sk.ainet.kotlinconf

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.s4_cnn.SampleDigits
import sk.ainet.kotlinconf.s4_cnn.classifyDigit
import sk.ainet.kotlinconf.s4_cnn.loadCnnWeights
import sk.ainet.kotlinconf.s4_cnn.mnistCnn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Stage 4, the real thing: load the pretrained `mnist_cnn.gguf` and confirm the CNN
 * classifies the embedded real MNIST test digits. (JVM-only because it reads the GGUF
 * from a classpath resource.)
 */
class CnnClassificationTest {

    private val ctx = DirectCpuExecutionContext.create()

    private fun weightBytes(): ByteArray =
        javaClass.getResourceAsStream("/mnist_cnn.gguf")?.readBytes()
            ?: error("mnist_cnn.gguf not found on the test classpath")

    @Test
    fun classifiesEveryEmbeddedDigit() {
        val model = mnistCnn(ctx)
        loadCnnWeights(model, weightBytes())

        for (label in SampleDigits.labels) {
            val result = classifyDigit(ctx, model, SampleDigits.pixels(label))
            assertEquals(label, result.digit, "misclassified the sample '$label'")
            assertTrue(result.confidence > 0.5f, "low confidence ${result.confidence} for '$label'")
        }
    }
}
