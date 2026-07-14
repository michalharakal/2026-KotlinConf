package sk.ainet.kotlinconf.tensors

import sk.ainet.context.ExecutionContext
import sk.ainet.lang.tensor.Tensor
import sk.ainet.lang.tensor.dsl.tensor
import sk.ainet.lang.tensor.sliceView
import sk.ainet.lang.types.FP32

/**
 * Tensors — "ML/AI Data structures" (slides: Scalar → Vector → Matrix → Tensor → Batch).
 *
 * Everything in SKaiNET is a [Tensor]. Rank (number of axes) is the only thing that
 * distinguishes a scalar from a batch — the DSL and the ops are identical across ranks.
 * The dtype (here [FP32]) is paired with a Kotlin value type (here `Float`) at compile time,
 * so `tensor<FP32, Int>` would not type-check.
 */

/** Scalar — a rank-1 tensor holding a single element. */
fun scalar(ctx: ExecutionContext): Tensor<FP32, Float> =
    tensor<FP32, Float>(ctx, FP32::class) {
        tensor { shape(1) { from(42f) } }
    }

/** Vector — a rank-1 tensor with N elements. */
fun vector(ctx: ExecutionContext): Tensor<FP32, Float> =
    tensor<FP32, Float>(ctx, FP32::class) {
        tensor { shape(4) { from(1f, 2f, 3f, 4f) } }
    }

/** Matrix — a rank-2 tensor (rows × cols). */
fun matrix(ctx: ExecutionContext): Tensor<FP32, Float> =
    tensor<FP32, Float>(ctx, FP32::class) {
        tensor {
            shape(2, 3) {
                from(
                    1f, 2f, 3f,
                    4f, 5f, 6f,
                )
            }
        }
    }

/** Tensor — rank-3 (channels × rows × cols), e.g. an RGB-like feature map. */
fun tensor3d(ctx: ExecutionContext): Tensor<FP32, Float> =
    tensor<FP32, Float>(ctx, FP32::class) {
        tensor { shape(3, 2, 2) { init { idx -> idx.sum().toFloat() } } }
    }

/** Batch — a leading batch axis is all that separates one sample from many (rank-4 here). */
fun batch(ctx: ExecutionContext): Tensor<FP32, Float> =
    tensor<FP32, Float>(ctx, FP32::class) {
        tensor { shape(8, 3, 2, 2) { randn(mean = 0f, std = 1f) } }
    }

/**
 * NumPy-style slicing via `sliceView { segment { ... } }` — one [segment] per rank.
 * Equivalent to numpy `t[0:2, 1, :, 0:6:2]` for a rank-4 tensor.
 */
fun numpySlice(ctx: ExecutionContext): Tensor<FP32, Float> {
    val big = tensor<FP32, Float>(ctx, FP32::class) {
        tensor { shape(4, 3, 2, 6) { init { idx -> idx.sum().toFloat() } } }
    }
    return big.sliceView {
        segment { range(0, 2) }   // dim 0: rows 0..1
        segment { at(1) }         // dim 1: pick index 1, collapse
        segment { all() }         // dim 2: everything
        segment { step(0, 6, 2) } // dim 3: 0, 2, 4
    }
}
