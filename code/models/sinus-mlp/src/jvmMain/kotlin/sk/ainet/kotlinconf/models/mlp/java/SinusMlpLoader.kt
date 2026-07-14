package sk.ainet.kotlinconf.models.mlp.java

import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import sk.ainet.context.ExecutionContext
import sk.ainet.kotlinconf.models.mlp.SinusMlp
import sk.ainet.kotlinconf.models.mlp.loadSinusMlp

/** Pure-Java entry point for the sin(x) MLP; mirrors `MnistCnnLoader`. */
class SinusMlpLoader @JvmOverloads constructor(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : AutoCloseable {

    fun loadAsync(ctx: ExecutionContext): CompletableFuture<SinusMlp> =
        scope.future { loadSinusMlp(ctx) }

    fun loadBlocking(ctx: ExecutionContext): SinusMlp =
        runBlocking { loadSinusMlp(ctx) }

    override fun close() {
        scope.cancel()
    }
}
