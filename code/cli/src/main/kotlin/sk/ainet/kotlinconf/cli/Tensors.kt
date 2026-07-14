package sk.ainet.kotlinconf.cli

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.tensors.batch
import sk.ainet.kotlinconf.tensors.matrix
import sk.ainet.kotlinconf.tensors.numpySlice
import sk.ainet.kotlinconf.tensors.scalar
import sk.ainet.kotlinconf.tensors.tensor3d
import sk.ainet.kotlinconf.tensors.vector

/** Tensors — ML/AI data structures. Run: `./gradlew :cli:runTensors`. */
fun main() {
    val ctx = DirectCpuExecutionContext.create()

    println("== Tensors · ML/AI data structures ==")
    println("scalar   shape = ${scalar(ctx).shape}")
    println("vector   shape = ${vector(ctx).shape}")
    println("matrix   shape = ${matrix(ctx).shape}")
    println("tensor3d shape = ${tensor3d(ctx).shape}")
    println("batch    shape = ${batch(ctx).shape}")
    println("sliced   shape = ${numpySlice(ctx).shape}   (numpy t[0:2, 1, :, 0:6:2])")
}
