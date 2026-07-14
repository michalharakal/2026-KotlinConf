package sk.ainet.kotlinconf.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sk.ainet.kotlinconf.models.transformer.TinyTransformerTrainer
import sk.ainet.kotlinconf.models.transformer.buildTinyTransformerTrainer

enum class TrainingPhase { Idle, Training, Done }

data class TransformerUiState(
    val phase: TrainingPhase = TrainingPhase.Idle,
    val epoch: Int = 0,
    val totalEpochs: Int = 120,
    val loss: Float = 0f,
    /** Loss per epoch, index = epoch − 1 — feeds the live loss plot. */
    val lossHistory: List<Float> = emptyList(),
    val prompt: String = "Der Hund",
    val predictions: List<Pair<String, Float>> = emptyList(),
)

/**
 * Trains the tiny decoder-only transformer live on-device **when the user taps Start**,
 * streaming the loss curve into the UI, then answers next-word queries.
 */
class TransformerViewModel : ViewModel() {

    private val _state = MutableStateFlow(TransformerUiState())
    val state: StateFlow<TransformerUiState> = _state.asStateFlow()

    private var trainer: TinyTransformerTrainer? = null
    private var job: Job? = null

    fun startTraining() {
        if (_state.value.phase == TrainingPhase.Training) return
        _state.value = TransformerUiState(phase = TrainingPhase.Training, prompt = _state.value.prompt)
        job = viewModelScope.launch {
            val losses = ArrayList<Float>()
            withContext(Dispatchers.Default) {
                val t = buildTinyTransformerTrainer()
                trainer = t
                t.train(epochs = _state.value.totalEpochs, learningRate = 0.05f).collect { p ->
                    losses.add(p.loss)
                    _state.value = _state.value.copy(epoch = p.epoch, loss = p.loss, lossHistory = losses.toList())
                }
            }
            _state.value = _state.value.copy(phase = TrainingPhase.Done)
            predict(_state.value.prompt)
        }
    }

    fun onPromptChange(text: String) { _state.value = _state.value.copy(prompt = text) }

    fun predict(prompt: String = _state.value.prompt) {
        if (_state.value.phase != TrainingPhase.Done) return
        viewModelScope.launch {
            val top = withContext(Dispatchers.Default) {
                trainer?.predictor()?.predictNext(prompt, k = 5)?.topK.orEmpty()
            }
            _state.value = _state.value.copy(predictions = top)
        }
    }
}
