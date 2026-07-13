package sk.ainet.kotlinconf.cli

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.s1_tensors.batch
import sk.ainet.kotlinconf.s1_tensors.matrix
import sk.ainet.kotlinconf.s1_tensors.numpySlice
import sk.ainet.kotlinconf.s1_tensors.scalar
import sk.ainet.kotlinconf.s1_tensors.tensor3d
import sk.ainet.kotlinconf.s1_tensors.vector

/** Stage 1 — ML/AI data structures. Run: `./gradlew :cli:runStage1`. */
fun main() {
    val ctx = DirectCpuExecutionContext.create()

    println("== Stage 1 · ML/AI data structures ==")
    println("scalar   shape = ${scalar(ctx).shape}")
    println("vector   shape = ${vector(ctx).shape}")
    println("matrix   shape = ${matrix(ctx).shape}")
    println("tensor3d shape = ${tensor3d(ctx).shape}")
    println("batch    shape = ${batch(ctx).shape}")
    println("sliced   shape = ${numpySlice(ctx).shape}   (numpy t[0:2, 1, :, 0:6:2])")
}
