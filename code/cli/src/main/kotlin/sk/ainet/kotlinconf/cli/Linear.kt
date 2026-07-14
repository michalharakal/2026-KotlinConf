package sk.ainet.kotlinconf.cli

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.linear.forwardNet
import sk.ainet.kotlinconf.linear.linearLayer
import sk.ainet.kotlinconf.linear.runForward

/** Linear — forward propagation. Run: `./gradlew :cli:runLinear`. */
fun main() {
    val ctx = DirectCpuExecutionContext.create()

    println("== Linear · Forward propagation in Kotlin ==")
    val y1 = runForward(ctx, linearLayer())
    println("single dense layer [1,3] -> ${y1.shape}")

    val y2 = runForward(ctx, forwardNet())
    println("2-hidden-layer MLP  [1,3] -> ${y2.shape}, output = ${y2.data[0, 0]}")
}
