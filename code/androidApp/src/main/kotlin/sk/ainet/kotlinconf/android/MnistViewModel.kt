package sk.ainet.kotlinconf.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.lang.nn.Module
import sk.ainet.lang.types.FP32
import sk.ainet.kotlinconf.models.cnn.DigitResult
import sk.ainet.kotlinconf.models.cnn.classifyDigit
import sk.ainet.kotlinconf.models.cnn.mnistCnnLoadFlow
import sk.ainet.kotlinconf.models.common.ModelLoadState

data class MnistUiState(
    val modelReady: Boolean = false,
    val busy: Boolean = false,
    val result: DigitResult? = null,
)

/**
 * Loads the pretrained CNN through the :models:mnist-cnn async loader (the weight file is a
 * bundled java-resource, read via kotlinx-io — no AssetManager), streaming load state via a
 * Flow, then runs the CNN on whatever the user draws — all on-device, off the main thread.
 */
class MnistViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx = DirectCpuExecutionContext.create()
    private var model: Module<FP32, Float>? = null

    private val _state = MutableStateFlow(MnistUiState())
    val state: StateFlow<MnistUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mnistCnnLoadFlow(ctx).collect { load ->
                when (load) {
                    is ModelLoadState.Loaded -> {
                        model = load.model
                        _state.value = _state.value.copy(modelReady = true)
                    }
                    is ModelLoadState.Failed -> _state.value = _state.value.copy(modelReady = false)
                    is ModelLoadState.Loading -> Unit
                }
            }
        }
    }

    fun classify(pixels: FloatArray) {
        val cnn = model
        if (_state.value.busy || cnn == null) return
        _state.value = _state.value.copy(busy = true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) { classifyDigit(ctx, cnn, pixels) }
            _state.value = _state.value.copy(busy = false, result = result)
        }
    }

    fun reset() { _state.value = _state.value.copy(result = null) }
}
