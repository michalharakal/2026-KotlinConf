package sk.ainet.kotlinconf.cli

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.s4_cnn.classifyDigit
import sk.ainet.kotlinconf.s4_cnn.mnistCnn

/** Stage 4 — CNN MNIST inference pipeline. Run: `./gradlew :cli:runStage4`. */
fun main() {
    val ctx = DirectCpuExecutionContext.create()
    val model = mnistCnn(ctx)

    // Synthetic "image": a vertical stroke down the middle (untrained weights, so the
    // prediction is arbitrary — the point is the shape pipeline 1×28×28 → 10 logits).
    val pixels = FloatArray(28 * 28) { i -> if ((i % 28) in 13..14) 1f else 0f }

    val (digit, logits) = classifyDigit(ctx, model, pixels)
    println("== Stage 4 · CNN MNIST (device-first) ==")
    println("input  shape = [1, 1, 28, 28]")
    println("logits shape = ${logits.shape}")
    println("argmax (untrained) = $digit")
    println("Load mnist_cnn.gguf weights for a real classifier — see docs/slide-to-code.md")
}
