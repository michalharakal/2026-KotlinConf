package sk.ainet.kotlinconf.cli

import kotlinx.coroutines.runBlocking
import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.models.cnn.SampleDigits
import sk.ainet.kotlinconf.models.cnn.classifyDigit
import sk.ainet.kotlinconf.models.cnn.loadMnistCnn

/** CNN — a real MNIST classifier, running on-device. Run: `./gradlew :cli:runCnn`. */
fun main() = runBlocking {
    val ctx = DirectCpuExecutionContext.create()

    // The :models:mnist-cnn module bundles mnist_cnn.gguf and loads it via kotlinx-io.
    val model = loadMnistCnn(ctx)

    println("== CNN · MNIST (device-first) ==")
    println("loaded the pretrained mnist_cnn.gguf into the LeNet CNN")
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
