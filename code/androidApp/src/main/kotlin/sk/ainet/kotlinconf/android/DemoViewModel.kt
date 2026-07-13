package sk.ainet.kotlinconf.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.s4_cnn.classifyDigit
import sk.ainet.kotlinconf.s4_cnn.mnistCnn
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_CONTEXT_LEN
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_CORPUS
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_MAX_VOCAB
import sk.ainet.kotlinconf.s5_transformer.TinyTransformerTrainer
import sk.ainet.kotlinconf.s5_transformer.WordTokenizer

data class DemoState(val busy: Boolean = false, val log: String = "Pick a stage to run.")

/**
 * Runs inference/training off the main thread on `Dispatchers.Default` and streams
 * results back through a [StateFlow] — the pattern from the SKaiNET Android skill.
 */
class DemoViewModel : ViewModel() {

    private val _state = MutableStateFlow(DemoState())
    val state: StateFlow<DemoState> = _state.asStateFlow()

    fun runCnn() = launchWork {
        val ctx = DirectCpuExecutionContext.create()
        val model = mnistCnn(ctx)
        val pixels = FloatArray(28 * 28) { if ((it % 28) in 13..14) 1f else 0f }
        val (digit, logits) = classifyDigit(ctx, model, pixels)
        "Stage 4 · CNN\ninput [1,1,28,28] → logits ${logits.shape}\nargmax (untrained) = $digit"
    }

    fun trainTransformer() = launchWork {
        val vocab = WordTokenizer.buildVocab(DEFAULT_CORPUS, DEFAULT_MAX_VOCAB)
        val windows = WordTokenizer.windows(DEFAULT_CORPUS, vocab, DEFAULT_CONTEXT_LEN)
        val trainer = TinyTransformerTrainer(vocab, windows, DEFAULT_CONTEXT_LEN)

        var last = 0f
        trainer.train(epochs = 120, learningRate = 0.05f).collect { last = it.loss }

        val preds = listOf("Der Hund", "Der Fisch", "Der Hamster").joinToString("\n") { prompt ->
            val top = trainer.predictor().predictNext(prompt, k = 3)?.topK.orEmpty()
                .joinToString { (w, p) -> "$w ${"%.2f".format(p)}" }
            "  \"$prompt\" → $top"
        }
        "Stage 5 · Transformer trained on-device\nfinal loss ${"%.4f".format(last)}\n$preds"
    }

    private fun launchWork(block: suspend () -> String) {
        if (_state.value.busy) return
        _state.value = _state.value.copy(busy = true, log = "Running…")
        viewModelScope.launch {
            val result = runCatching { withContext(Dispatchers.Default) { block() } }
            _state.value = DemoState(
                busy = false,
                log = result.getOrElse { "Error: ${it.message}" },
            )
        }
    }
}
