package sk.ainet.kotlinconf.cli

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import sk.ainet.kotlinconf.models.transformer.DEFAULT_CORPUS
import sk.ainet.kotlinconf.models.transformer.buildTinyTransformerTrainer

/**
 * Transformer — train a decoder-only transformer live, then predict the next word.
 * Run: `./gradlew :cli:runTransformer`. Trains in a couple of seconds on the CPU backend.
 */
fun main() = runBlocking {
    println("== Transformer · decoder-only, trained live ==")

    val trainer = buildTinyTransformerTrainer()
    val vocab = trainer.vocab

    println("corpus = ${DEFAULT_CORPUS.size} sentences, vocab = ${vocab.sizeWithoutSpecials} words, windows = ${trainer.windows.size}")
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
