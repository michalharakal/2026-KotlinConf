package sk.ainet.kotlinconf.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Stage 4 + Stage 5, running on-device. The SKaiNET model code is the exact same
 * `sk.ainet.kotlinconf.*` shared code the `:cli` runs on the JVM — this is the
 * "device-first, one codebase" thesis of the talk made concrete.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DemoScreen()
                }
            }
        }
    }
}

@Composable
private fun DemoScreen(vm: DemoViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("SKaiNET · KotlinConf 2026", style = MaterialTheme.typography.headlineSmall)
        Text("Redefining ML with Kotlin — on-device", style = MaterialTheme.typography.bodyMedium)

        Button(onClick = { vm.runCnn() }, enabled = !state.busy) {
            Text("Stage 4 · Run CNN inference pipeline")
        }
        Button(onClick = { vm.trainTransformer() }, enabled = !state.busy) {
            Text("Stage 5 · Train tiny transformer")
        }

        Text(state.log, style = MaterialTheme.typography.bodyMedium)
    }
}
