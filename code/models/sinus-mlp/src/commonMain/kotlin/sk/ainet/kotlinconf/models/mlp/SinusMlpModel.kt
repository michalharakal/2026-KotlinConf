package sk.ainet.kotlinconf.models.mlp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import sk.ainet.context.ExecutionContext
import sk.ainet.kotlinconf.models.common.ModelLoadState

/**
 * Builds the sin(x) MLP off the caller's thread ([Dispatchers.Default]). The pretrained
 * weights are baked into `skainet-lang-models`, so there is no file I/O — but the async API
 * keeps the surface consistent with the other model modules and structured-concurrency safe.
 */
suspend fun loadSinusMlp(ctx: ExecutionContext): SinusMlp =
    withContext(Dispatchers.Default) { SinusMlp(ctx) }

/** [loadSinusMlp] as a progress [Flow]: `Loading(0f) → Loaded(mlp)`, or `Failed(error)`. */
fun sinusMlpLoadFlow(ctx: ExecutionContext): Flow<ModelLoadState<SinusMlp>> = flow {
    emit(ModelLoadState.Loading(0f))
    val mlp = SinusMlp(ctx)
    emit(ModelLoadState.Loaded(mlp))
}.catch { emit(ModelLoadState.Failed(it)) }.flowOn(Dispatchers.Default)
