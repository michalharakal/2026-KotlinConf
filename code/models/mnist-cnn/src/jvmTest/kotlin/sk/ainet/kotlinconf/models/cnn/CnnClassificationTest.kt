package sk.ainet.kotlinconf.models.cnn

import kotlinx.coroutines.runBlocking
import sk.ainet.context.DirectCpuExecutionContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The real thing: load the bundled `mnist_cnn.gguf` through the module's own async API and
 * confirm the CNN classifies the embedded real MNIST test digits. This also proves the
 * resource wiring — the single `src/weights/mnist_cnn.gguf` is on the JVM test classpath.
 */
class CnnClassificationTest {

    private val ctx = DirectCpuExecutionContext.create()

    @Test
    fun classifiesEveryEmbeddedDigit() = runBlocking {
        val model = loadMnistCnn(ctx)   // exercises openModelResource + loadCnnWeights

        for (label in SampleDigits.labels) {
            val result = classifyDigit(ctx, model, SampleDigits.pixels(label))
            assertEquals(label, result.digit, "misclassified the sample '$label'")
            assertTrue(result.confidence > 0.5f, "low confidence ${result.confidence} for '$label'")
        }
    }
}
