package sk.ainet.kotlinconf.s2_linear

import sk.ainet.context.ExecutionContext
import sk.ainet.lang.nn.Module
import sk.ainet.lang.nn.dsl.sequential
import sk.ainet.lang.tensor.Tensor
import sk.ainet.lang.tensor.dsl.tensor
import sk.ainet.lang.tensor.relu
import sk.ainet.lang.types.FP32

/**
 * Stage 2 — "Forward Propagation in Kotlin" (slide: the `class Linear` / hidden layer).
 *
 * A `dense(N)` layer computes `y = x · Wᵀ + b`. On the slide this is hand-written as a
 * `Linear` class calling `matmul`; with SKaiNET the same thing is one line of DSL, and a
 * whole feed-forward network is just a stack of them. `forward(x, ctx)` runs the pass.
 */

/** A single linear (dense) layer: `[*, 3] -> [*, 2]`. This *is* a matmul (+ bias). */
fun linearLayer(): Module<FP32, Float> = sequential {
    input(3)
    dense(2)
}

/** A 2-hidden-layer MLP — forward propagation through several layers with ReLU between them. */
fun forwardNet(): Module<FP32, Float> = sequential {
    input(3)
    dense(4)
    activation { it.relu() }
    dense(2)
    activation { it.relu() }
    dense(1)
}

/** Build a `[1, 3]` input row and push it through [model]. */
fun runForward(ctx: ExecutionContext, model: Module<FP32, Float>): Tensor<FP32, Float> {
    val x = tensor<FP32, Float>(ctx, FP32::class) {
        tensor { shape(1, 3) { from(0.1f, 0.2f, 0.3f) } }
    }
    return model.forward(x, ctx)
}
