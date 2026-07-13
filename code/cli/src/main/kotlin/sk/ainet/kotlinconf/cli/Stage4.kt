package sk.ainet.kotlinconf.cli

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.s4_cnn.SampleDigits
import sk.ainet.kotlinconf.s4_cnn.classifyDigit
import sk.ainet.kotlinconf.s4_cnn.loadCnnWeights
import sk.ainet.kotlinconf.s4_cnn.mnistCnn

/** Stage 4 — a real CNN MNIST classifier, running on-device. Run: `./gradlew :cli:runStage4`. */
fun main() {
    val ctx = DirectCpuExecutionContext.create()
    val model = mnistCnn(ctx)

    // Load the pretrained weights from the bundled GGUF file (classpath resource).
    val weights = object {}.javaClass.getResourceAsStream("/mnist_cnn.gguf")
        ?.readBytes() ?: error("mnist_cnn.gguf not found on the classpath")
    loadCnnWeights(model, weights)

    println("== Stage 4 · CNN MNIST (device-first) ==")
    println("loaded mnist_cnn.gguf (${weights.size} bytes) into the LeNet CNN")
    println()

    // Classify one real MNIST test digit of each label, all fully offline.
    var correct = 0
    for (label in SampleDigits.labels) {
        val result = classifyDigit(ctx, model, SampleDigits.pixels(label))
        if (result.digit == label) correct++
        val pct = (result.confidence * 100).toInt()
        val mark = if (result.digit == label) "✓" else "✗"
        println("  actual $label → predicted ${result.digit}  (${pct}% conf)  $mark")
    }
    println()
    println("accuracy on the 10 embedded samples: $correct / ${SampleDigits.labels.size}")
    println()
    println("A drawn digit:")
    print(SampleDigits.ascii(SampleDigits.labels.first()))
}
