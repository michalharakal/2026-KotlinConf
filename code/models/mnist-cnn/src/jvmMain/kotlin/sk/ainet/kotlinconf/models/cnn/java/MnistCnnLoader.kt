package sk.ainet.kotlinconf.models.cnn.java

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sk.ainet.context.ExecutionContext
import sk.ainet.kotlinconf.models.cnn.loadMnistCnn
import sk.ainet.kotlinconf.models.cnn.mnistCnnLoadFlow
import sk.ainet.kotlinconf.models.common.ModelLoadState
import sk.ainet.lang.nn.Module
import sk.ainet.lang.types.FP32

/**
 * Pure-Java entry point for the bundled MNIST CNN, matching SKaiNET's `sk.ainet.java` facade
 * convention. Java callers cannot use Kotlin `suspend`/`Flow` directly, so this bridges the
 * coroutine API to `CompletableFuture`, a blocking call, and a callback.
 *
 * ```java
 * MnistCnnLoader loader = new MnistCnnLoader();
 * loader.loadAsync(ctx).thenAccept(model -> { ... });
 * ```
 *
 * Owns a background [CoroutineScope]; call [close] when done to cancel any in-flight work.
 */
class MnistCnnLoader @JvmOverloads constructor(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : AutoCloseable {

    /** Asynchronous load; completes with a ready-to-run model (or completes exceptionally). */
    fun loadAsync(ctx: ExecutionContext): CompletableFuture<Module<FP32, Float>> =
        scope.future { loadMnistCnn(ctx) }

    /** Blocking load — convenient for simple `main()` / non-reactive Java code. */
    fun loadBlocking(ctx: ExecutionContext): Module<FP32, Float> =
        runBlocking { loadMnistCnn(ctx) }

    /**
     * Streams load progress to [onState]. Returns an [AutoCloseable] that cancels the load
     * when closed.
     */
    fun load(
        ctx: ExecutionContext,
        onState: Consumer<ModelLoadState<Module<FP32, Float>>>,
    ): AutoCloseable {
        val job = scope.launch { mnistCnnLoadFlow(ctx).collect { onState.accept(it) } }
        return AutoCloseable { job.cancel() }
    }

    override fun close() {
        scope.cancel()
    }
}
