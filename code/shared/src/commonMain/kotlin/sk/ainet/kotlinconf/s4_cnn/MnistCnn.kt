package sk.ainet.kotlinconf.s4_cnn

import sk.ainet.context.ExecutionContext
import sk.ainet.lang.nn.Module
import sk.ainet.lang.nn.dsl.sequential
import sk.ainet.lang.tensor.Shape
import sk.ainet.lang.tensor.Tensor
import sk.ainet.lang.tensor.relu
import sk.ainet.lang.types.FP32

/**
 * Stage 4 — a convolutional network for MNIST, the "device-first" model.
 *
 * This is the LeNet-style architecture the slides sketch: two conv+ReLU+maxpool blocks,
 * a flatten, then a dense classifier to 10 logits. It is expressed entirely with the
 * SKaiNET NN DSL and runs the same on JVM and Android (the `:androidApp` surfaces it
 * on-device). Here we build the model and run the inference *pipeline* on a synthetic
 * `[1, 1, 28, 28]` image — the shapes flowing through each layer are the teaching point.
 *
 * To turn this into a real classifier, load pre-trained weights (the SKaiNET examples
 * ship `mnist_cnn.gguf`) with the `skainet-io-gguf` / `skainet-io-safetensors` loaders
 * and feed a real 28×28 grayscale image — see docs/slide-to-code.md.
 */

/** Two conv blocks → flatten → dense(10). Input `[batch, 1, 28, 28]`, output `[batch, 10]`. */
fun mnistCnn(ctx: ExecutionContext): Module<FP32, Float> = sequential(ctx) {
    // Declaring the input shape lets the dense layer infer its 1568 in-features
    // after the conv/pool/flatten chain.
    input(intArrayOf(1, 28, 28))

    // Conv block 1: 1 → 16 channels, 5×5 kernel, padding 2  →  16 × 28 × 28
    conv2d("conv1") {
        inChannels = 1
        outChannels = 16
        kernelSize(5)
        stride(1)
        padding(2)
    }
    activation { it.relu() }
    maxPool2d(kernelSize = 2 to 2, stride = 2 to 2)   // → 16 × 14 × 14

    // Conv block 2: 16 → 32 channels                    → 32 × 14 × 14
    conv2d("conv2") {
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

/** Run the CNN inference pipeline on a synthetic 28×28 image → 10 logits, and argmax it. */
fun classifyDigit(ctx: ExecutionContext, model: Module<FP32, Float>, pixels: FloatArray): Pair<Int, Tensor<FP32, Float>> {
    require(pixels.size == 28 * 28) { "expected 784 pixels, got ${pixels.size}" }
    val input = ctx.fromFloatArray<FP32, Float>(Shape(1, 1, 28, 28), FP32::class, pixels)
    val logits = model.forward(input, ctx)
    var best = 0
    var bestVal = Float.NEGATIVE_INFINITY
    for (j in 0 until 10) {
        val v = logits.data[0, j]
        if (v > bestVal) { bestVal = v; best = j }
    }
    return best to logits
}
