package sk.ainet.kotlinconf.models.transformer.java

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import sk.ainet.kotlinconf.models.transformer.DEFAULT_CORPUS
import sk.ainet.kotlinconf.models.transformer.DEFAULT_EPOCHS
import sk.ainet.kotlinconf.models.transformer.DEFAULT_LEARNING_RATE
import sk.ainet.kotlinconf.models.transformer.Predictor
import sk.ainet.kotlinconf.models.transformer.TrainingProgress
import sk.ainet.kotlinconf.models.transformer.buildTinyTransformerTrainer

/**
 * Pure-Java entry point for the live-trained decoder-only transformer. Java callers cannot
 * consume the Kotlin `Flow<TrainingProgress>` directly, so this bridges training to a
 * `CompletableFuture` and a progress callback.
 *
 * ```java
 * TinyTransformerLoader loader = new TinyTransformerLoader();
 * loader.trainAsync().thenAccept(predictor -> predictor.predictNext("Der Hund", 5));
 * ```
 */
class TinyTransformerLoader @JvmOverloads constructor(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : AutoCloseable {

    /** Trains to completion and completes with a ready [Predictor]. */
    @JvmOverloads
    fun trainAsync(
        corpus: List<String> = DEFAULT_CORPUS,
        epochs: Int = DEFAULT_EPOCHS,
        learningRate: Float = DEFAULT_LEARNING_RATE,
    ): CompletableFuture<Predictor> = scope.future {
        val trainer = buildTinyTransformerTrainer(corpus)
        trainer.train(epochs, learningRate).collect { /* drain to completion */ }
        trainer.predictor()
    }

    /**
     * Trains while streaming each epoch's [TrainingProgress] to [onProgress]. Returns an
     * [AutoCloseable] that cancels training (the Stop button).
     */
    @JvmOverloads
    fun train(
        onProgress: Consumer<TrainingProgress>,
        corpus: List<String> = DEFAULT_CORPUS,
        epochs: Int = DEFAULT_EPOCHS,
        learningRate: Float = DEFAULT_LEARNING_RATE,
    ): AutoCloseable {
        val job = scope.launch {
            buildTinyTransformerTrainer(corpus).train(epochs, learningRate)
                .collect { onProgress.accept(it) }
        }
        return AutoCloseable { job.cancel() }
    }

    override fun close() {
        scope.cancel()
    }
}
