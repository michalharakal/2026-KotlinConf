package sk.ainet.kotlinconf

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.s1_tensors.batch
import sk.ainet.kotlinconf.s1_tensors.matrix
import sk.ainet.kotlinconf.s1_tensors.numpySlice
import sk.ainet.kotlinconf.s1_tensors.scalar
import sk.ainet.kotlinconf.s1_tensors.vector
import sk.ainet.kotlinconf.s2_linear.forwardNet
import sk.ainet.kotlinconf.s2_linear.linearLayer
import sk.ainet.kotlinconf.s2_linear.runForward
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import sk.ainet.kotlinconf.s3_mlp.SinusMlp
import sk.ainet.kotlinconf.s4_cnn.classifyDigit
import sk.ainet.kotlinconf.s4_cnn.mnistCnn
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_CONTEXT_LEN
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_CORPUS
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_MAX_VOCAB
import sk.ainet.kotlinconf.s5_transformer.TinyTransformerTrainer
import sk.ainet.kotlinconf.s5_transformer.WordTokenizer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StagesTest {

    private val ctx = DirectCpuExecutionContext.create()

    @Test
    fun stage1_shapes() {
        assertEquals(listOf(1), scalar(ctx).shape.dimensions.toList())
        assertEquals(listOf(4), vector(ctx).shape.dimensions.toList())
        assertEquals(listOf(2, 3), matrix(ctx).shape.dimensions.toList())
        assertEquals(listOf(8, 3, 2, 2), batch(ctx).shape.dimensions.toList())
        // numpy t[0:2, 1, :, 0:6:2] on a [4,3,2,6] tensor -> [2, 2, 3]
        assertEquals(listOf(2, 2, 3), numpySlice(ctx).shape.dimensions.toList())
    }

    @Test
    fun stage2_forwardShapes() {
        assertEquals(listOf(1, 2), runForward(ctx, linearLayer()).shape.dimensions.toList())
        assertEquals(listOf(1, 1), runForward(ctx, forwardNet()).shape.dimensions.toList())
    }

    @Test
    fun stage3_approximatesSine() {
        val mlp = SinusMlp(ctx)
        var maxErr = 0f
        var x = 0f
        while (x <= (PI / 2).toFloat()) {
            val err = abs(sin(x.toDouble()).toFloat() - mlp.predict(x))
            maxErr = maxOf(maxErr, err)
            x += 0.1f
        }
        // Pretrained net should track sin(x) closely across [0, π/2].
        assertTrue(maxErr < 0.05f, "max abs error $maxErr exceeded tolerance")
    }

    @Test
    fun stage4_cnnPipeline() {
        val model = mnistCnn(ctx)
        val pixels = FloatArray(28 * 28) { if ((it % 28) in 13..14) 1f else 0f }
        val result = classifyDigit(ctx, model, pixels)
        // Pipeline produces a 10-way probability distribution that sums to ~1.
        assertEquals(10, result.probabilities.size)
        assertTrue(result.digit in 0..9)
        assertTrue(abs(result.probabilities.sum() - 1f) < 1e-3f)
    }

    @Test
    fun stage5_transformerTrainsAndPredicts() = runTest {
        val vocab = WordTokenizer.buildVocab(DEFAULT_CORPUS, DEFAULT_MAX_VOCAB)
        val windows = WordTokenizer.windows(DEFAULT_CORPUS, vocab, DEFAULT_CONTEXT_LEN)
        val trainer = TinyTransformerTrainer(vocab, windows, DEFAULT_CONTEXT_LEN)

        val progress = trainer.train(epochs = 60, learningRate = 0.05f).toList()
        // Training should reduce the loss substantially over the run.
        assertTrue(progress.last().loss < progress.first().loss * 0.6f,
            "loss did not converge: ${progress.first().loss} -> ${progress.last().loss}")

        val top = trainer.predictor().predictNext("Der Hund", k = 3)?.topK.orEmpty()
        assertTrue(top.isNotEmpty(), "predictor returned no candidates")
    }
}
