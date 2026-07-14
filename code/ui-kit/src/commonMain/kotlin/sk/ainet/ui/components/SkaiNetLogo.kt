package sk.ainet.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import sk.ainet.ui.generated.resources.Res
import sk.ainet.ui.generated.resources.skainet_logo

/**
 * The official SKaiNET logo (the red-ring node-triangle mark), loaded from this module's
 * bundled Compose resource — a real image file under `src/commonMain/composeResources/drawable/`.
 */
@Composable
fun SkaiNetLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.skainet_logo),
        contentDescription = "SKaiNET",
        modifier = modifier,
    )
}
