package sk.ainet.kotlinconf.cli

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_CONTEXT_LEN
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_CORPUS
import sk.ainet.kotlinconf.s5_transformer.DEFAULT_MAX_VOCAB
import sk.ainet.kotlinconf.s5_transformer.TinyTransformerTrainer
import sk.ainet.kotlinconf.s5_transformer.WordTokenizer

/**
 * Stage 5 — train a decoder-only transformer live, then predict the next word.
 * Run: `./gradlew :cli:runStage5`. Trains in a couple of seconds on the CPU backend.
 */
fun main() = runBlocking {
    println("== Stage 5 · Tiny decoder-only transformer (trained live) ==")

    val vocab = WordTokenizer.buildVocab(DEFAULT_CORPUS, DEFAULT_MAX_VOCAB)
    val windows = WordTokenizer.windows(DEFAULT_CORPUS, vocab, DEFAULT_CONTEXT_LEN)
    val trainer = TinyTransformerTrainer(vocab, windows, DEFAULT_CONTEXT_LEN)

    println("corpus = ${DEFAULT_CORPUS.size} sentences, vocab = ${vocab.sizeWithoutSpecials} words, windows = ${windows.size}")
    println("training …")

    val epochs = 120
    trainer.train(epochs = epochs, learningRate = 0.05f).collect { p ->
        if (p.epoch == 1 || p.epoch % 30 == 0 || p.isCompleted) {
            println("  epoch ${p.epoch.toString().padStart(3)} · loss ${"%.4f".format(p.loss)}")
        }
    }

    val predictor = trainer.predictor()
    for (prompt in listOf("Der Hund", "Der Fisch", "Der Hamster")) {
        val top = predictor.predictNext(prompt, k = 3)?.topK.orEmpty()
        val rendered = top.joinToString { (w, prob) -> "$w ${"%.2f".format(prob)}" }
        println("  \"$prompt\" → next: $rendered")
    }
}
