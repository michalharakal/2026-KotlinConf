package sk.ainet.kotlinconf.web

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sk.ainet.kotlinconf.models.transformer.TinyTransformerTrainer
import sk.ainet.kotlinconf.models.transformer.buildTinyTransformerTrainer
import sk.ainet.ui.components.LoadingIndicator
import sk.ainet.ui.components.SkaiNetLogo
import sk.ainet.ui.plot.AxisConfig
import sk.ainet.ui.plot.DataPoint
import sk.ainet.ui.plot.DataSeries
import sk.ainet.ui.plot.GridConfig
import sk.ainet.ui.plot.LinePlot
import sk.ainet.ui.plot.PlotBounds

// SKaiNET design system — dark, near-black canvas with the signature red accent
// (mirrors examples.skainet.sk and the Android demo's theme).
private val SkaiColors = darkColorScheme(
    primary = Color(0xFFDC2626),
    onPrimary = Color.White,
    background = Color(0xFF0A0B0D),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF0F1114),
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF1A1D21),
    onSurfaceVariant = Color(0xFF9AA1AC),
    outline = Color(0xFF262B31),
)

private const val TOTAL_EPOCHS = 120

private enum class Phase { Idle, Training, Done }

@Composable
fun App() {
    MaterialTheme(colorScheme = SkaiColors) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                    Header()
                    Spacer(Modifier.height(20.dp))
                    TransformerDemo()
                    Spacer(Modifier.height(24.dp))
                    RepoFooter()
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Text(
        "SKaiNET · KotlinConf",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "Transformer, trained live in your browser",
        fontSize = 26.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        "A decoder-only transformer, written in pure Kotlin with the SKaiNET DSL, compiled to " +
            "WebAssembly and trained from scratch here — no server, no download.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TransformerDemo() {
    var phase by remember { mutableStateOf(Phase.Idle) }
    var epoch by remember { mutableStateOf(0) }
    var loss by remember { mutableStateOf(0f) }
    var lossHistory by remember { mutableStateOf<List<DataPoint>>(emptyList()) }
    var prompt by remember { mutableStateOf("Der Hund") }
    var predictions by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    var trainer by remember { mutableStateOf<TinyTransformerTrainer?>(null) }
    val scope = rememberCoroutineScope()

    fun predict() {
        val t = trainer ?: return
        predictions = t.predictor().predictNext(prompt, k = 5)?.topK.orEmpty()
    }

    fun startTraining() {
        phase = Phase.Training
        epoch = 0
        loss = 0f
        lossHistory = emptyList()
        predictions = emptyList()
        scope.launch {
            val t = buildTinyTransformerTrainer()
            trainer = t
            // train() yields periodically, keeping the single-threaded browser UI responsive
            // so the loss plot animates as it drops.
            t.train(epochs = TOTAL_EPOCHS, learningRate = 0.05f).collect { p ->
                epoch = p.epoch
                loss = p.loss
                lossHistory = lossHistory + DataPoint(p.epoch.toFloat(), p.loss)
            }
            phase = Phase.Done
            predict()
        }
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp)) {
            when (phase) {
                Phase.Idle -> IdleSplash(onStart = ::startTraining)
                Phase.Training -> TrainingView(epoch, loss, lossHistory)
                Phase.Done -> DoneView(
                    lossHistory = lossHistory,
                    prompt = prompt,
                    predictions = predictions,
                    onPromptChange = { prompt = it },
                    onPredict = { scope.launch { predict() } },
                    onRetrain = ::startTraining,
                )
            }
        }
    }
}

/** Branded landing: the official SKaiNET mark (orbiting triangle) over a Start button. */
@Composable
private fun IdleSplash(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SkaiNetLogo(Modifier.size(120.dp))
        Spacer(Modifier.height(20.dp))
        Text(
            "Train a decoder-only transformer from scratch on a tiny German corpus — right here, " +
                "in a couple of seconds.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("Start training")
        }
    }
}

@Composable
private fun TrainingView(epoch: Int, loss: Float, lossHistory: List<DataPoint>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LoadingIndicator(size = 28.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            "Training on-device…  epoch $epoch/$TOTAL_EPOCHS  ·  loss ${fmt(loss)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    Spacer(Modifier.height(16.dp))
    LossPlot(lossHistory)
}

@Composable
private fun DoneView(
    lossHistory: List<DataPoint>,
    prompt: String,
    predictions: List<Pair<String, Float>>,
    onPromptChange: (String) -> Unit,
    onPredict: () -> Unit,
    onRetrain: () -> Unit,
) {
    LossPlot(lossHistory)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = prompt,
        onValueChange = onPromptChange,
        label = { Text("Prompt") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth()) {
        Button(onClick = onPredict, modifier = Modifier.weight(1f)) { Text("Predict next word") }
        Spacer(Modifier.width(12.dp))
        OutlinedButton(onClick = onRetrain) { Text("Train again") }
    }
    if (predictions.isNotEmpty()) {
        Spacer(Modifier.height(20.dp))
        Text(
            "Next word after \"$prompt\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(10.dp))
        val top = predictions.firstOrNull()?.first
        for ((word, p) in predictions) {
            ProbabilityBar(word, p, highlight = word == top)
        }
    }
}

/** Live training-loss curve, drawn with the vendored SKaiNET plot API. */
@Composable
private fun LossPlot(history: List<DataPoint>) {
    val yMax = (history.maxOfOrNull { it.y } ?: 3f).coerceAtLeast(0.1f)
    LinePlot(
        series = listOf(
            DataSeries(
                points = history,
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2f,
            ),
        ),
        modifier = Modifier.fillMaxWidth().height(180.dp),
        bounds = PlotBounds(0f, TOTAL_EPOCHS.toFloat(), 0f, yMax),
        xAxis = AxisConfig(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            labelFormatter = { it.toInt().toString() },
        ),
        yAxis = AxisConfig(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            labelFormatter = { fmt(it) },
        ),
        grid = GridConfig(color = MaterialTheme.colorScheme.outline),
    )
}

@Composable
private fun ProbabilityBar(label: String, value: Float, highlight: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            )
            Text(
                "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/**
 * Page-flow call to action: SKaiNET is open source — a clear, always-visible link to the repo,
 * styled with the red accent so it reads as an invitation rather than fine print.
 */
@Composable
private fun RepoFooter() {
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        Text(
            "SKaiNET is a from-scratch machine-learning framework in Kotlin Multiplatform.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Star it on GitHub  ·  github.com/SKaiNET-developers/SKaiNET",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                uriHandler.openUri("https://github.com/SKaiNET-developers/SKaiNET")
            },
        )
    }
}

private fun fmt(v: Float): String {
    val scaled = (v * 1000).toInt()
    return "${scaled / 1000}.${(scaled % 1000).toString().padStart(3, '0')}"
}
