# SKaiNET · KotlinConf 2026 — code

Runnable companion to the talk **"Redefining Machine Learning with Kotlin: A Device-First
Approach to AI."** It walks the talk's arc as five **patterns** — Tensors, Linear, MLP, CNN,
Transformer — each using the real [SKaiNET](https://github.com/SKaiNET-developers/SKaiNET) DSL.
The same Kotlin model code runs on the **JVM, Android, and the browser (WebAssembly)** — the
talk's device-first, one-codebase thesis, made concrete.

> **Pinned versions.** This repo is pinned to **SKaiNET 0.36.0** and **Kotlin 2.4.0** (SKaiNET's
> browser klibs are built with Kotlin 2.4.0, so the web target needs it; Compose Multiplatform
> 1.11.x pairs with it). For newer versions, check the official
> [SKaiNET examples repository](https://github.com/SKaiNET-developers/SKaiNET-examples).

See [`../docs/slide-to-code.md`](../docs/slide-to-code.md) to line each pattern up with the slides.

## Layout

```
code/
  shared/       KMP (JVM + Android) — the two foundational demos
    tensors/    data structures: scalar → vector → matrix → tensor → batch, slicing
    linear/     forward propagation: dense layers + forward(x, ctx)
  models/       standalone, publishable model modules (group sk.ainet.kotlinconf.models)
    model-common/     ModelLoadState (jvm + android + wasmJs)
    sinus-mlp/        MLP approximating sin(x) (pretrained, from skainet-lang-models)
    mnist-cnn/        LeNet CNN for MNIST — bundles the one mnist_cnn.gguf (jvm + android)
    tiny-transformer/ decoder-only transformer, trained live (jvm + android + wasmJs)
  cli/          JVM entry points — one runnable per pattern
  androidApp/   Compose app: draw-a-digit (CNN) + next-word (transformer) on-device
  webApp/       Compose/WebAssembly app: the transformer, trained live in the browser
```

## Run the patterns (JVM)

```bash
./gradlew :cli:runTensors       # tensors & slicing
./gradlew :cli:runLinear        # forward propagation
./gradlew :cli:runMlp           # MLP: y = sin(x)
./gradlew :cli:runCnn           # CNN: classify real MNIST digits (pretrained weights)
./gradlew :cli:runTransformer   # train a tiny transformer, then predict
```

`runCnn` loads `mnist_cnn.gguf` (from the `:models:mnist-cnn` module) and classifies real
embedded MNIST test digits — 10/10 correct. `runTransformer` trains a decoder-only transformer
from scratch in ~120 epochs (a couple of seconds on CPU), dropping the loss from ~2.9 to ~0.6.

## The DSL, by example

**Define data (tensors).** Everything is a `Tensor`; rank is the only thing separating a scalar
from a batch, and slicing is NumPy-style — one `segment` per rank
([`tensors/Tensors.kt`](shared/src/commonMain/kotlin/sk/ainet/kotlinconf/tensors/Tensors.kt)):

```kotlin
val matrix = tensor<FP32, Float>(ctx, FP32::class) {
    tensor { shape(2, 3) { from(1f, 2f, 3f, 4f, 5f, 6f) } }
}

// numpy t[0:2, 1, :, 0:6:2] on a rank-4 tensor
val sliced = big.sliceView {
    segment { range(0, 2) }   // dim 0: rows 0..1
    segment { at(1) }         // dim 1: pick index 1, collapse
    segment { all() }         // dim 2: everything
    segment { step(0, 6, 2) } // dim 3: 0, 2, 4
}
```

**Define a model.** A network is a type-safe stack of layers; `forward(x, ctx)` runs the pass
([`mnist-cnn/MnistCnn.kt`](models/mnist-cnn/src/commonMain/kotlin/sk/ainet/kotlinconf/models/cnn/MnistCnn.kt)):

```kotlin
val cnn = sequential<FP32, Float>(ctx) {
    input(intArrayOf(1, 28, 28))
    conv2d("stage1.conv1") { inChannels = 1;  outChannels = 16; kernelSize(5); padding(2) }
    activation { it.relu() }
    maxPool2d(kernelSize = 2 to 2, stride = 2 to 2)
    conv2d("stage2.conv2") { inChannels = 16; outChannels = 32; kernelSize(5); padding(2) }
    activation { it.relu() }
    maxPool2d(kernelSize = 2 to 2, stride = 2 to 2)
    flatten()
    dense(10, "out")
}
val logits = cnn.forward(x, ctx)   // [1, 1, 28, 28] -> [1, 10]
```

## Standalone model modules

Each model is a **standalone, reusable Gradle module** that can be published and consumed by any
JVM/Android/Kotlin app — not just this demo.

- **One weight file, every platform.** `:models:mnist-cnn` bundles a *single* `mnist_cnn.gguf`
  (in `src/weights/`) that Gradle packages as a java-resource into both the JVM jar and the
  Android AAR. It is read cross-platform via **kotlinx-io + `expect`/`actual`** — no Compose
  Resources, no duplicated copies, no `AssetManager`.
- **Async loading API.** Every module exposes a structured-concurrency loader and a `Flow` of
  load progress:

  ```kotlin
  val cnn = loadMnistCnn(ctx)                        // suspend, runs on Dispatchers.Default
  mnistCnnLoadFlow(ctx).collect { state ->           // Loading(fraction) → Loaded(model) → Failed
      when (state) { is ModelLoadState.Loaded -> use(state.model); else -> Unit }
  }
  ```

- **Java-compatible facade.** Pure-Java callers use the `*Loader` classes in the `…​.java`
  package:

  ```java
  MnistCnnLoader loader = new MnistCnnLoader();
  loader.loadAsync(ctx).thenAccept(model -> classify(model, pixels));
  ```

- **Publish & consume.** `./gradlew publishToMavenLocal` publishes
  `sk.ainet.kotlinconf.models:{mnist-cnn, sinus-mlp, tiny-transformer, model-common}:0.1.0`
  (root + jvm + android [+ wasm-js]). Another project can then:

  ```kotlin
  repositories { mavenLocal() }
  dependencies { implementation("sk.ainet.kotlinconf.models:mnist-cnn:0.1.0") }
  ```

## Tests

```bash
./gradlew check   # foundations (tensors/linear) + per-module CNN/MLP/transformer tests
```

## Android

```bash
./gradlew :androidApp:assembleDebug   # builds the on-device demo APK
```

Two **interactive, on-device** demos, styled to match the SKaiNET examples
(examples.skainet.sk) — dark-first with the signature red accent:

| Draw a digit (CNN) | Next word (Transformer) |
| --- | --- |
| ![mnist](../docs/images/app-mnist.png) | ![transformer](../docs/images/app-transformer.png) |

- **Draw a digit** — scribble a number; the LeNet CNN (real `mnist_cnn.gguf` weights, loaded from
  the `:models:mnist-cnn` module via the async `mnistCnnLoadFlow`) classifies it on-device.
- **Next word** — a decoder-only transformer trains live on-device (loss streamed into the UI),
  then predicts the next word for any prompt you type.

## Web (WebAssembly)

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun   # serves the demo at http://localhost:8080
./gradlew :webApp:wasmJsBrowserDistribution     # static bundle in build/dist/wasmJs
```

The **same `:models:tiny-transformer` code** compiled to WebAssembly — the decoder-only
transformer trains from scratch in the browser (no server, no download), then predicts the next
word:

![web transformer](../docs/images/app-web-transformer.png)

## Notes

- **BOM vs libraries:** the BOM is group `sk.ainet` (`sk.ainet:skainet-bom`); the libraries are
  group `sk.ainet.core` (`sk.ainet.core:skainet-*`). See `gradle/libs.versions.toml`.
- Convention plugin `kotlinconf.model-module` (in `build-logic/`) gives every model module its
  KMP targets + `maven-publish` config.
- Learn more: [docs](https://skainet-developers.github.io/SKaiNET/) ·
  [live demos](https://examples.skainet.sk) ·
  [examples repo](https://github.com/SKaiNET-developers/SKaiNET-examples).
