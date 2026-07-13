# SKaiNET · KotlinConf 2026 — code

Runnable companion to the talk **"Redefining Machine Learning with Kotlin: A Device-First
Approach to AI."** It walks the talk's arc as five progressive stages, each using the real
[SKaiNET](https://github.com/SKaiNET-developers/SKaiNET) DSL (pinned to **0.36.0**, from Maven
Central). The same Kotlin model code runs on the JVM and on Android — the talk's device-first,
one-codebase thesis, made concrete.

See [`../docs/slide-to-code.md`](../docs/slide-to-code.md) to line each stage up with the slides.

## Layout

```
code/
  shared/     KMP (JVM + Android) — all model code, one package per stage
    s1_tensors/      data structures: scalar → vector → matrix → tensor → batch
    s2_linear/       forward propagation: dense layers + forward(x, ctx)
    s3_mlp/          MLP approximating sin(x) (pretrained weights)
    s4_cnn/          LeNet-style CNN for MNIST (device-first)
    s5_transformer/  decoder-only transformer, trained live
  cli/        JVM entry points — one runnable per stage
  androidApp/ Compose app running Stage 4 + Stage 5 on-device
```

## Requirements

- JDK 21 (the build uses a Gradle toolchain; the wrapper pulls Gradle 9.5.1).
- For `:androidApp`: an Android SDK. `local.properties` points at it via `sdk.dir`.

## Run the stages (JVM)

```bash
./gradlew :cli:runStage1   # tensors & slicing
./gradlew :cli:runStage2   # forward propagation
./gradlew :cli:runStage3   # MLP: y = sin(x)
./gradlew :cli:runStage4   # CNN MNIST inference pipeline
./gradlew :cli:runStage5   # train a tiny transformer, then predict
```

Stage 5 sample output:

```
corpus = 6 sentences, vocab = 16 words, windows = 24
training …
  epoch 120 · loss 0.5813
  "Der Hund"    → next: laut 0.31, frisst 0.23, bellt 0.20
  "Der Hamster" → next: rennt 0.28, mag 0.25, ruhig 0.08
```

## Tests

```bash
./gradlew :shared:jvmTest   # shape/output assertions for all five stages
```

## Android

```bash
./gradlew :androidApp:assembleDebug   # builds the on-device demo APK
```

The app runs the exact same `sk.ainet.kotlinconf.*` shared code, off the main thread on
`Dispatchers.Default`, streaming results back through a `StateFlow` (the pattern from the
SKaiNET Android integration guide).

## Notes

- **BOM vs libraries:** the BOM is group `sk.ainet` (`sk.ainet:skainet-bom`); the libraries are
  group `sk.ainet.core` (`sk.ainet.core:skainet-*`). See `gradle/libs.versions.toml`.
- **Kotlin version** is pinned to the one SKaiNET 0.36.0 was built against (2.3.21).
- Learn more: [docs](https://skainet-developers.github.io/SKaiNET/) ·
  [live demos](https://examples.skainet.sk) ·
  [examples repo](https://github.com/SKaiNET-developers/SKaiNET-examples).
