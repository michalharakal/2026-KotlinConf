package sk.ainet.kotlinconf.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_CONTEXT_LEN
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_CORPUS
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_MAX_VOCAB
import sk.ainet.kotlinconf.s5_transformer.TinyTransformerTrainer
import sk.ainet.kotlinconf.s5_transformer.WordTokenizer

data class TransformerUiState(
    val training: Boolean = true,
    val epoch: Int = 0,
    val totalEpochs: Int = 120,
    val loss: Float = 0f,
    val ready: Boolean = false,
    val prompt: String = "Der Hund",
    val predictions: List<Pair<String, Float>> = emptyList(),
)

/**
 * Trains the tiny decoder-only transformer live on-device (a couple of seconds), streaming
 * loss into the UI, then answers next-word queries for whatever prompt the user types.
 */
class TransformerViewModel : ViewModel() {

    private val _state = MutableStateFlow(TransformerUiState())
    val state: StateFlow<TransformerUiState> = _state.asStateFlow()

    private lateinit var trainer: TinyTransformerTrainer

    init {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val vocab = WordTokenizer.buildVocab(DEFAULT_CORPUS, DEFAULT_MAX_VOCAB)
                val windows = WordTokenizer.windows(DEFAULT_CORPUS, vocab, DEFAULT_CONTEXT_LEN)
                trainer = TinyTransformerTrainer(vocab, windows, DEFAULT_CONTEXT_LEN)
                var epoch = 0
                trainer.train(epochs = _state.value.totalEpochs, learningRate = 0.05f).collect { p ->
                    epoch++
                    _state.value = _state.value.copy(epoch = epoch, loss = p.loss)
                }
            }
            _state.value = _state.value.copy(training = false, ready = true)
            predict(_state.value.prompt)
        }
    }

    fun onPromptChange(text: String) { _state.value = _state.value.copy(prompt = text) }

    fun predict(prompt: String = _state.value.prompt) {
        if (!_state.value.ready) return
        viewModelScope.launch {
            val top = withContext(Dispatchers.Default) {
                trainer.predictor().predictNext(prompt, k = 5)?.topK.orEmpty()
            }
            _state.value = _state.value.copy(predictions = top)
        }
    }
}
