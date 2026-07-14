package sk.ainet.kotlinconf.models.cnn

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import sk.ainet.context.ExecutionContext
import sk.ainet.kotlinconf.models.common.ModelLoadState
import sk.ainet.lang.nn.Module
import sk.ainet.lang.types.FP32

/** The bundled weight file, shipped once in this module's `src/weights/`. */
private const val WEIGHTS = "mnist_cnn.gguf"

/**
 * Builds the LeNet CNN and loads the pretrained `mnist_cnn.gguf` weights, off the caller's
 * thread ([Dispatchers.Default]). Structured-concurrency friendly: cancelling the calling
 * coroutine cancels the load. Returns a ready-to-run [Module].
 */
suspend fun loadMnistCnn(ctx: ExecutionContext): Module<FP32, Float> =
    withContext(Dispatchers.Default) {
        mnistCnn(ctx).also { loadCnnWeights(it, readModelResourceBytes(WEIGHTS)) }
    }

/**
 * Same load as [loadMnistCnn], but as a cold [Flow] of [ModelLoadState] so a UI can show
 * progress: `Loading(0f) → Loading(…) → Loaded(model)`, or `Failed(error)` on any error.
 * Work runs on [Dispatchers.Default]; collect it from any scope.
 */
fun mnistCnnLoadFlow(ctx: ExecutionContext): Flow<ModelLoadState<Module<FP32, Float>>> = flow {
    emit(ModelLoadState.Loading(0f))
    val model = mnistCnn(ctx)
    emit(ModelLoadState.Loading(0.3f))
    val bytes = readModelResourceBytes(WEIGHTS)
    emit(ModelLoadState.Loading(0.7f))
    loadCnnWeights(model, bytes)
    emit(ModelLoadState.Loaded(model))
}.catch { emit(ModelLoadState.Failed(it)) }.flowOn(Dispatchers.Default)
