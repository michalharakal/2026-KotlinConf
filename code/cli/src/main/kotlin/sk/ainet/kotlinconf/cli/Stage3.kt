package sk.ainet.kotlinconf.cli

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.s3_mlp.SinusMlp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** Stage 3 — MLP approximating y = sin(x). Run: `./gradlew :cli:runStage3`. */
fun main() {
    val ctx = DirectCpuExecutionContext.create()
    val mlp = SinusMlp(ctx)

    println("== Stage 3 · MLP function approximator (y = sin x) ==")
    println("   x      sin(x)     MLP(x)     |err|")
    var maxErr = 0f
    var x = 0f
    while (x <= (PI / 2).toFloat() + 1e-4f) {
        val truth = sin(x.toDouble()).toFloat()
        val pred = mlp.predict(x)
        val err = abs(truth - pred)
        maxErr = maxOf(maxErr, err)
        println("  ${"%.3f".format(x)}   ${"%.5f".format(truth)}   ${"%.5f".format(pred)}   ${"%.5f".format(err)}")
        x += (PI / 8).toFloat()
    }
    println("max abs error over [0, π/2] = ${"%.5f".format(maxErr)}")
}
