package sk.ainet.kotlinconf.android

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import sk.ainet.kotlinconf.android.ui.DigitCanvas
import sk.ainet.kotlinconf.android.ui.rememberDrawState
import sk.ainet.kotlinconf.android.ui.toMnistPixels

private enum class Screen { HOME, MNIST, TRANSFORMER }

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (screen) {
            Screen.HOME -> HomeScreen(onOpen = { screen = it })
            Screen.MNIST -> MnistScreen(onBack = { screen = Screen.HOME })
            Screen.TRANSFORMER -> TransformerScreen(onBack = { screen = Screen.HOME })
        }
    }
}

/* ----------------------------------------------------------------------- Home */

@Composable
private fun HomeScreen(onOpen: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        BrandHeader()
        Spacer(Modifier.height(24.dp))
        Text(
            "On-device AI, pure Kotlin. Pick a demo — everything below runs locally with SKaiNET.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        DemoCard(
            title = "Draw a digit",
            subtitle = "CNN",
            body = "Scribble a number; a LeNet CNN with pretrained weights recognises it on-device.",
            onClick = { onOpen(Screen.MNIST) },
        )
        Spacer(Modifier.height(14.dp))
        DemoCard(
            title = "Next word",
            subtitle = "Transformer",
            body = "A decoder-only transformer trained live in seconds, then predicts the next word.",
            onClick = { onOpen(Screen.TRANSFORMER) },
        )
        Spacer(Modifier.weight(1f))
        Text(
            "KotlinConf 2026 · Redefining ML with Kotlin",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text("S", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "SKaiNET",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                "device-first machine learning",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DemoCard(title: String, subtitle: String, body: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* --------------------------------------------------------------------- Shared */

@Composable
private fun ScreenScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Back", color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(4.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

/** A labelled probability bar filling to [fraction] of the width, in the SKaiNET red. */
@Composable
private fun ProbabilityBar(label: String, fraction: Float, highlight: Boolean) {
    val animated by animateFloatAsState(fraction, label = "prob")
    val barColor by animateColorAsState(
        if (highlight) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
        label = "barColor",
    )
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.width(64.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
        )
        Box(
            Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(Modifier.fillMaxWidth(animated.coerceIn(0f, 1f)).height(18.dp).clip(RoundedCornerShape(9.dp)).background(barColor))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "${(fraction * 100).toInt()}%",
            modifier = Modifier.width(44.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/* ---------------------------------------------------------------------- MNIST */

@Composable
private fun MnistScreen(onBack: () -> Unit, vm: MnistViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val draw = rememberDrawState()
    val density = LocalDensity.current

    ScreenScaffold("Draw a digit", onBack) {
        Text(
            if (state.modelReady) "Draw a single digit (0–9), then tap Classify."
            else "Loading mnist_cnn.gguf…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            DigitCanvas(draw, size = 280.dp)
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    val px = with(density) { 280.dp.toPx().toInt() }
                    vm.classify(draw.toMnistPixels(px))
                },
                enabled = state.modelReady && !state.busy && !draw.isEmpty,
                modifier = Modifier.weight(1f),
            ) { Text("Classify") }
            OutlinedButton(
                onClick = { draw.clear(); vm.reset() },
                modifier = Modifier.weight(1f),
            ) { Text("Clear") }
        }
        Spacer(Modifier.height(20.dp))

        val result = state.result
        if (result != null) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${result.digit}",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "${(result.confidence * 100).toInt()}% confident",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    for (d in 0..9) {
                        ProbabilityBar("digit $d", result.probabilities[d], highlight = d == result.digit)
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------------- Transformer */

@Composable
private fun TransformerScreen(onBack: () -> Unit, vm: TransformerViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    ScreenScaffold("Next word", onBack) {
        if (state.training) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Training on-device…  epoch ${state.epoch}/${state.totalEpochs}  ·  loss ${"%.3f".format(state.loss)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "A decoder-only transformer is learning a tiny German corpus from scratch, right now, on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@ScreenScaffold
        }

        Text(
            "Trained (final loss ${"%.3f".format(state.loss)}). Type a prompt; the model predicts the next word.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.prompt,
            onValueChange = vm::onPromptChange,
            label = { Text("Prompt") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.predict() }, modifier = Modifier.fillMaxWidth()) {
            Text("Predict next word")
        }
        Spacer(Modifier.height(20.dp))
        if (state.predictions.isNotEmpty()) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "\"${state.prompt}\" →",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(10.dp))
                    val top = state.predictions.firstOrNull()?.first
                    for ((word, p) in state.predictions) {
                        ProbabilityBar(word, p, highlight = word == top)
                    }
                }
            }
        }
    }
}
