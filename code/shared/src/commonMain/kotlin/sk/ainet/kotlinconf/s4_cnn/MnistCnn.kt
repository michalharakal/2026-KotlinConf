package sk.ainet.kotlinconf.s4_cnn

import kotlin.math.exp
import sk.ainet.context.ExecutionContext
import sk.ainet.lang.nn.Module
import sk.ainet.lang.nn.dsl.sequential
import sk.ainet.lang.tensor.Shape
import sk.ainet.lang.tensor.relu
import sk.ainet.lang.types.FP32

/**
 * Stage 4 — a convolutional network for MNIST, the "device-first" model.
 *
 * This is the LeNet-style architecture the slides sketch: two conv+ReLU+maxpool blocks,
 * a flatten, then a dense classifier to 10 logits. It is expressed entirely with the
 * SKaiNET NN DSL and runs the same on JVM and Android (the `:androidApp` surfaces it
 * on-device, and `:cli:runStage4` on the JVM).
 *
 * Unlike the earlier stages, this one is a *real, trained* classifier: [loadCnnWeights]
 * fills the layers from the pretrained `mnist_cnn.gguf`, and [classifyDigit] then returns
 * an actual prediction. The layer names below (`stage1.conv1`, `stage2.conv2`, `out`) are
 * deliberately matched to the tensor names inside that GGUF file.
 */

/** The result of a classification: the predicted digit and the full 0..9 probability vector. */
data class DigitResult(val digit: Int, val probabilities: FloatArray) {
    /** Confidence of the winning digit, in 0f..1f. */
    val confidence: Float get() = probabilities[digit]

    override fun equals(other: Any?): Boolean =
        this === other || (other is DigitResult && digit == other.digit &&
            probabilities.contentEquals(other.probabilities))

    override fun hashCode(): Int = 31 * digit + probabilities.contentHashCode()
}

/** Two conv blocks → flatten → dense(10). Input `[batch, 1, 28, 28]`, output `[batch, 10]`. */
fun mnistCnn(ctx: ExecutionContext): Module<FP32, Float> = sequential(ctx) {
    // Declaring the input shape lets the dense layer infer its 1568 in-features
    // after the conv/pool/flatten chain.
    input(intArrayOf(1, 28, 28))

    // Conv block 1: 1 → 16 channels, 5×5 kernel, padding 2  →  16 × 28 × 28
    conv2d("stage1.conv1") {
        inChannels = 1
        outChannels = 16
        kernelSize(5)
        stride(1)
        padding(2)
    }
    activation { it.relu() }
    maxPool2d(kernelSize = 2 to 2, stride = 2 to 2)   // → 16 × 14 × 14

    // Conv block 2: 16 → 32 channels                    → 32 × 14 × 14
    conv2d("stage2.conv2") {
        inChannels = 16
        outChannels = 32
        kernelSize(5)
        stride(1)
        padding(2)
    }
    activation { it.relu() }
    maxPool2d(kernelSize = 2 to 2, stride = 2 to 2)   // → 32 × 7 × 7

    flatten()                                          // → 1568
    dense(10, "out")                                   // → 10 logits
}

/** A plain-MLP baseline for the same task: `[batch, 784] → 10`. */
fun mnistMlp(ctx: ExecutionContext): Module<FP32, Float> = sequential(ctx) {
    input(784)
    dense(128, "fc1") { weights { randn(std = 0.1f) } }
    activation { it.relu() }
    dense(10, "fc2") { weights { randn(std = 0.1f) } }
}

/**
 * Run the CNN on a 28×28 grayscale image (`pixels` = 784 floats in 0f..1f, row-major),
 * returning the predicted digit and a softmax probability over 0..9.
 */
fun classifyDigit(ctx: ExecutionContext, model: Module<FP32, Float>, pixels: FloatArray): DigitResult {
    require(pixels.size == 28 * 28) { "expected 784 pixels, got ${pixels.size}" }
    val input = ctx.fromFloatArray<FP32, Float>(Shape(1, 1, 28, 28), FP32::class, pixels)
    val logits = model.forward(input, ctx)
    val raw = FloatArray(10) { logits.data[0, it] }
    return DigitResult(argmax(raw), softmax(raw))
}

private fun argmax(v: FloatArray): Int {
    var best = 0
    for (i in v.indices) if (v[i] > v[best]) best = i
    return best
}

private fun softmax(logits: FloatArray): FloatArray {
    val max = logits.max()
    val exps = FloatArray(logits.size) { exp((logits[it] - max).toDouble()).toFloat() }
    val sum = exps.sum()
    return FloatArray(exps.size) { exps[it] / sum }
}
