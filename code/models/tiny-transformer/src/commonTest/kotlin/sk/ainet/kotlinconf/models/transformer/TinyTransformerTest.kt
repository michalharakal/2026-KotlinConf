package sk.ainet.kotlinconf.models.transformer

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TinyTransformerTest {

    @Test
    fun trainsAndPredicts() = runTest {
        val trainer = buildTinyTransformerTrainer()

        val progress = trainer.train(epochs = 60, learningRate = 0.05f).toList()
        // Training should reduce the loss substantially over the run.
        assertTrue(
            progress.last().loss < progress.first().loss * 0.6f,
            "loss did not converge: ${progress.first().loss} -> ${progress.last().loss}",
        )

        val top = trainer.predictor().predictNext("Der Hund", k = 3)?.topK.orEmpty()
        assertTrue(top.isNotEmpty(), "predictor returned no candidates")
    }
}
