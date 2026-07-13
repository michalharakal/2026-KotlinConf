package sk.ainet.kotlinconf.cli

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.s2_linear.forwardNet
import sk.ainet.kotlinconf.s2_linear.linearLayer
import sk.ainet.kotlinconf.s2_linear.runForward

/** Stage 2 — forward propagation. Run: `./gradlew :cli:runStage2`. */
fun main() {
    val ctx = DirectCpuExecutionContext.create()

    println("== Stage 2 · Forward propagation in Kotlin ==")
    val y1 = runForward(ctx, linearLayer())
    println("single dense layer [1,3] -> ${y1.shape}")

    val y2 = runForward(ctx, forwardNet())
    println("2-hidden-layer MLP  [1,3] -> ${y2.shape}, output = ${y2.data[0, 0]}")
}
