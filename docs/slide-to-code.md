# Slide → Code map

The [`code/`](../code) project is organised to follow the talk. Each pattern in the deck maps to
code you can run, so you can watch the same idea go from a slide to running Kotlin. Every pattern
uses the **real SKaiNET 0.36.0 DSL** — no from-scratch reimplementations — and runs unchanged on
the JVM (`:cli`), on Android (`:androidApp`), and, for the transformer, in the browser
(`:webApp`, WebAssembly).

| Talk section (slides) | Concept | Code | Run |
| --- | --- | --- | --- |
| "Let's build GPT from scratch", Decoder-only (GPT-2) | Why on-device, what we're building toward | *(motivation — realised by the Transformer)* | — |
| **ML/AI data structures** — Scalar → Vector → Matrix → Tensor → Batch | Tensors, ranks, dtypes, NumPy-style slicing | [`shared/.../tensors/Tensors.kt`](../code/shared/src/commonMain/kotlin/sk/ainet/kotlinconf/tensors/Tensors.kt) | `./gradlew :cli:runTensors` |
| **Forward Propagation in Kotlin** — the `Linear` / hidden layer | `dense` layer = matmul + bias; `forward(x, ctx)` | [`shared/.../linear/Linear.kt`](../code/shared/src/commonMain/kotlin/sk/ainet/kotlinconf/linear/Linear.kt) | `./gradlew :cli:runLinear` |
| **Forward propagation** (the `A₂ = X₁W₁₂ + X₂W₂₂` network) | First end-to-end model: MLP approximating `sin x` | [`models/sinus-mlp/.../SinusMlp.kt`](../code/models/sinus-mlp/src/commonMain/kotlin/sk/ainet/kotlinconf/models/mlp/SinusMlp.kt) | `./gradlew :cli:runMlp` |
| **From 5 fps to 30 fps** / on-device performance | LeNet-style CNN classifying real digits from pretrained weights | [`models/mnist-cnn/.../MnistCnn.kt`](../code/models/mnist-cnn/src/commonMain/kotlin/sk/ainet/kotlinconf/models/cnn/MnistCnn.kt) | `./gradlew :cli:runCnn` |
| **Decoder only** (GPT-2 vs a 10M model) | Decoder-only transformer, **trained live** | [`models/tiny-transformer/`](../code/models/tiny-transformer/src/commonMain/kotlin/sk/ainet/kotlinconf/models/transformer/) | `./gradlew :cli:runTransformer` |
| **One language to find them** — coroutines & Multiplatform | Same code on JVM + Android + Web; interactive on-device demos, inference off the main thread | [`androidApp/.../AppRoot.kt`](../code/androidApp/src/main/kotlin/sk/ainet/kotlinconf/android/AppRoot.kt) · [`webApp/.../App.kt`](../code/webApp/src/wasmJsMain/kotlin/sk/ainet/kotlinconf/web/App.kt) | `:androidApp:assembleDebug` · `:webApp:wasmJsBrowserDevelopmentRun` |
| **Kotlin DSL** — data defs / pipelines / architectures → compile → execute | The "ML/AI as Code" pipeline, tied together | all patterns above | `./gradlew check` |

## What each pattern demonstrates

- **Tensors.** Rank is the only thing separating a scalar from a batch. Build each with
  `tensor<FP32, Float>(ctx, FP32::class) { tensor { shape(...) { ... } } }` and slice with
  `sliceView { segment { ... } }` (one `segment` per rank).
- **Linear.** A `dense(N)` layer is `y = x·Wᵀ + b`; a network is a
  `sequential { input(...); dense(...); activation { it.relu() } }` stack; `forward(x, ctx)`
  runs the pass.
- **MLP.** The KotlinConf sinus demo: architecture `1 → 16 → 16 → 1` declared with
  `definition { network(ctx) { ... } }`, weights loaded from the pretrained
  `SinusApproximatorWandB` in `skainet-lang-models`. Max error vs `Math.sin` over `[0, π/2]`
  is < 0.01. Lives in the standalone `:models:sinus-mlp` module.
- **CNN.** Two conv+ReLU+maxpool blocks → flatten → `dense(10)`, `[1,1,28,28] → [1,10]`. A
  **real classifier**: `loadCnnWeights` fills the layers from the pretrained `mnist_cnn.gguf`
  (via `skainet-io-gguf`), and `classifyDigit` returns a prediction with a softmax confidence.
  The layer names (`stage1.conv1`, `stage2.conv2`, `out`) are chosen to match the tensor names
  in the GGUF file — **do not rename them**. Lives in the standalone `:models:mnist-cnn` module,
  which bundles the single `mnist_cnn.gguf` and loads it cross-platform via kotlinx-io.
- **Transformer.** A decoder-only transformer (token + positional embeddings → causal
  self-attention → projection) built as a custom `Module`, **trained from scratch** on a
  six-sentence corpus in ~120 epochs (a couple of seconds on CPU). Loss drops from ~2.9 to ~0.6
  and it predicts sensible next words. This is the "train a transformer live" moment from the
  deck, running in pure Kotlin — on the JVM, on Android, and in the browser on WebAssembly.
  Lives in the standalone `:models:tiny-transformer` module.

Slide numbers are approximate — the deck is 75 slides and section boundaries move between
revisions; the mapping is by section, not exact page.
