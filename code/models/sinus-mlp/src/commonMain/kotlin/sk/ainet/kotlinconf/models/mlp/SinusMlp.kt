package sk.ainet.kotlinconf.models.mlp

import sk.ainet.context.ExecutionContext
import sk.ainet.lang.model.dnn.mlp.pretrained.SinusApproximatorWandB
import sk.ainet.lang.nn.Module
import sk.ainet.lang.nn.definition
import sk.ainet.lang.nn.network
import sk.ainet.lang.tensor.dsl.tensor
import sk.ainet.lang.tensor.relu
import sk.ainet.lang.types.FP32

/**
 * MLP — the first end-to-end model: an MLP that approximates `y = sin(x)` on `[0, π/2]`.
 *
 * This mirrors the actual KotlinConf demo. The architecture (1 → 16 → 16 → 1, ReLU) is
 * declared with the `definition { network(ctx) { ... } }` DSL, and the layer weights/biases
 * come pre-trained from [SinusApproximatorWandB] shipped in `skainet-lang-models` — so the
 * model is deterministic and needs no training loop to run.
 */
class SinusMlp(private val ctx: ExecutionContext) {

    private val wandb = SinusApproximatorWandB()

    val model: Module<FP32, Float> = definition<FP32, Float> {
        network(ctx) {
            input(1, "input")
            dense(16, "hidden-1") {
                weights { fromArray(wandb.getLayer1WandB("").weights) }
                bias { fromArray(wandb.getLayer1WandB("").bias) }
                activation = { it.relu() }
            }
            dense(16, "hidden-2") {
                weights { fromArray(wandb.getLayer2WandB("").weights) }
                bias { fromArray(wandb.getLayer2WandB("").bias) }
                activation = { it.relu() }
            }
            dense(1, "output") {
                weights { fromArray(wandb.getLayer3WandB("").weights) }
                bias { fromArray(wandb.getLayer3WandB("").bias) }
            }
        }
    }

    /** Predict sin(x) for a single scalar angle (radians, in `[0, π/2]`). */
    fun predict(angle: Float): Float {
        val input = tensor<FP32, Float>(ctx, FP32::class) {
            tensor { shape(1, 1) { from(angle) } }
        }
        return model.forward(input, ctx).data[0, 0]
    }
}
