# Slide → Code map

The [`code/`](../code) project is organised to follow the talk. Each stage of the deck maps
to a package in `code/shared` and a runnable in `code/cli`, so you can watch the same idea go
from a slide to running Kotlin. Every stage uses the **real SKaiNET 0.36.0 DSL** — no
from-scratch reimplementations — and runs unchanged on the JVM (`:cli`) and on Android
(`:androidApp`).

| Talk section (slides) | Concept | Code | Run |
| --- | --- | --- | --- |
| "Let's build GPT from scratch", Decoder-only (GPT-2) | Why on-device, what we're building toward | *(motivation — realised by Stage 5)* | — |
| **ML/AI data structures** — Scalar → Vector → Matrix → Tensor → Batch | Tensors, ranks, dtypes, NumPy-style slicing | [`shared/.../s1_tensors/Tensors.kt`](../code/shared/src/commonMain/kotlin/sk/ainet/kotlinconf/s1_tensors/Tensors.kt) | `./gradlew :cli:runStage1` |
| **Forward Propagation in Kotlin** — the `Linear` / hidden layer | `dense` layer = matmul + bias; `forward(x, ctx)` | [`shared/.../s2_linear/Linear.kt`](../code/shared/src/commonMain/kotlin/sk/ainet/kotlinconf/s2_linear/Linear.kt) | `./gradlew :cli:runStage2` |
| **Forward propagation** (the `A₂ = X₁W₁₂ + X₂W₂₂` network) | First end-to-end model: MLP approximating `sin x` | [`shared/.../s3_mlp/SinusMlp.kt`](../code/shared/src/commonMain/kotlin/sk/ainet/kotlinconf/s3_mlp/SinusMlp.kt) | `./gradlew :cli:runStage3` |
| **From 5 fps to 30 fps** / on-device performance | LeNet-style CNN classifying real digits from pretrained weights | [`shared/.../s4_cnn/MnistCnn.kt`](../code/shared/src/commonMain/kotlin/sk/ainet/kotlinconf/s4_cnn/MnistCnn.kt) | `./gradlew :cli:runStage4` |
| **Decoder only** (GPT-2 vs a 10M model) | Decoder-only transformer, **trained live** | [`shared/.../s5_transformer/`](../code/shared/src/commonMain/kotlin/sk/ainet/kotlinconf/s5_transformer/) | `./gradlew :cli:runStage5` |
| **One language to find them** — coroutines & Multiplatform | Same code on JVM + Android; two interactive on-device demos, inference off the main thread | [`androidApp/.../AppRoot.kt`](../code/androidApp/src/main/kotlin/sk/ainet/kotlinconf/android/AppRoot.kt) | `./gradlew :androidApp:assembleDebug` |
| **Kotlin DSL** — data defs / pipelines / architectures → compile → execute | The "ML/AI as Code" pipeline, tied together | all stages above | `./gradlew :shared:jvmTest` |

## What each stage demonstrates

- **Stage 1 — tensors.** Rank is the only thing separating a scalar from a batch. Build each
  with `tensor<FP32, Float>(ctx, FP32::class) { tensor { shape(...) { ... } } }` and slice with
  `sliceView { segment { ... } }` (one `segment` per rank).
- **Stage 2 — forward propagation.** A `dense(N)` layer is `y = x·Wᵀ + b`; a network is a
  `sequential { input(...); dense(...); activation { it.relu() } }` stack; `forward(x, ctx)`
  runs the pass.
- **Stage 3 — MLP.** The KotlinConf sinus demo: architecture `1 → 16 → 16 → 1` declared with
  `definition { network(ctx) { ... } }`, weights loaded from the pretrained
  `SinusApproximatorWandB` in `skainet-lang-models`. Max error vs `Math.sin` over `[0, π/2]`
  is < 0.01.
- **Stage 4 — CNN.** Two conv+ReLU+maxpool blocks → flatten → `dense(10)`, `[1,1,28,28] →
  [1,10]`. A **real classifier**: `loadCnnWeights` fills the layers from the pretrained
  `mnist_cnn.gguf` (via `skainet-io-gguf`), and `classifyDigit` returns a prediction with a
  softmax confidence. The CLI classifies real embedded MNIST samples (10/10 correct); the
  Android app classifies whatever you draw. The layer names (`stage1.conv1`, `stage2.conv2`,
  `out`) are chosen to match the tensor names in the GGUF file.
- **Stage 5 — transformer.** A decoder-only transformer (token + positional embeddings →
  causal self-attention → projection) built as a custom `Module`, **trained from scratch** on a
  six-sentence corpus in ~120 epochs (a couple of seconds on CPU). Loss drops from ~2.9 to ~0.6
  and it predicts sensible next words. This is the "train a transformer live" moment from the
  deck, running in pure Kotlin.

Slide numbers are approximate — the deck is 75 slides and section boundaries move between
revisions; the mapping is by section, not exact page.
