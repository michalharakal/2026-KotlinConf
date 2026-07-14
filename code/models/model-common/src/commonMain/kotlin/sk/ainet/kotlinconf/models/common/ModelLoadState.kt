package sk.ainet.kotlinconf.models.common

/**
 * Progress of an asynchronous model load, emitted by the `*LoadFlow(...)` APIs of the
 * standalone model modules.
 *
 * A typical stream is `Loading(0f) → Loading(…) → Loaded(model)`, or `… → Failed(error)`.
 * The type parameter [M] is the loaded model (e.g. a SKaiNET `Module`), kept generic so this
 * module stays dependency-free and usable on every target, including the browser (wasmJs).
 */
sealed interface ModelLoadState<out M> {
    /** Work is in progress; [fraction] advances from 0f to 1f (best-effort, not exact). */
    data class Loading(val fraction: Float) : ModelLoadState<Nothing>

    /** The model is ready to run. */
    data class Loaded<M>(val model: M) : ModelLoadState<M>

    /** Loading failed; [error] is the cause. */
    data class Failed(val error: Throwable) : ModelLoadState<Nothing>
}
