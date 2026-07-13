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
import sk.ainet.kotlinconf.s4_cnn.DigitResult
import sk.ainet.kotlinconf.s4_cnn.classifyDigit
import sk.ainet.kotlinconf.s4_cnn.loadCnnWeights
import sk.ainet.kotlinconf.s4_cnn.mnistCnn

data class MnistUiState(
    val modelReady: Boolean = false,
    val busy: Boolean = false,
    val result: DigitResult? = null,
)

/**
 * Loads the pretrained `mnist_cnn.gguf` from the app assets (once), then runs the CNN on
 * whatever the user draws — all on-device, off the main thread via `Dispatchers.Default`.
 */
class MnistViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx = DirectCpuExecutionContext.create()
    private val model = mnistCnn(ctx)

    private val _state = MutableStateFlow(MnistUiState())
    val state: StateFlow<MnistUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val bytes = getApplication<Application>().assets.open("mnist_cnn.gguf").use { it.readBytes() }
                loadCnnWeights(model, bytes)
            }
            _state.value = _state.value.copy(modelReady = true)
        }
    }

    fun classify(pixels: FloatArray) {
        if (_state.value.busy || !_state.value.modelReady) return
        _state.value = _state.value.copy(busy = true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) { classifyDigit(ctx, model, pixels) }
            _state.value = _state.value.copy(busy = false, result = result)
        }
    }

    fun reset() { _state.value = _state.value.copy(result = null) }
}
