package sk.ainet.kotlinconf.web

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sk.ainet.kotlinconf.models.transformer.TinyTransformerTrainer
import sk.ainet.kotlinconf.models.transformer.buildTinyTransformerTrainer

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

@Composable
fun App() {
    MaterialTheme(colorScheme = SkaiColors) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                    Header()
                    Spacer(Modifier.height(20.dp))
                    TransformerDemo()
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
    var training by remember { mutableStateOf(true) }
    var epoch by remember { mutableStateOf(0) }
    var loss by remember { mutableStateOf(0f) }
    var prompt by remember { mutableStateOf("Der Hund") }
    var predictions by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    var trainer by remember { mutableStateOf<TinyTransformerTrainer?>(null) }
    val scope = rememberCoroutineScope()

    fun predict() {
        val t = trainer ?: return
        predictions = t.predictor().predictNext(prompt, k = 5)?.topK.orEmpty()
    }

    // Train once on entry. train() yields periodically, keeping the single-threaded
    // browser UI responsive so the loss animates as it drops.
    LaunchedEffect(Unit) {
        val t = buildTinyTransformerTrainer()
        trainer = t
        t.train(epochs = TOTAL_EPOCHS, learningRate = 0.05f).collect { p ->
            epoch = p.epoch
            loss = p.loss
        }
        training = false
        predict()
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp)) {
            if (training) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Training on-device…  epoch $epoch/$TOTAL_EPOCHS  ·  loss ${fmt(loss)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { epoch / TOTAL_EPOCHS.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            } else {
                Text(
                    "Trained (final loss ${fmt(loss)}). Type a prompt; the model predicts the next word.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Prompt") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { scope.launch { predict() } },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Predict next word") }

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
        }
    }
}

@Composable
private fun ProbabilityBar(label: String, value: Float, highlight: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
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

private fun fmt(v: Float): String {
    val scaled = (v * 1000).toInt()
    return "${scaled / 1000}.${(scaled % 1000).toString().padStart(3, '0')}"
}
