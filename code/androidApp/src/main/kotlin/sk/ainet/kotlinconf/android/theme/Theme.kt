package sk.ainet.kotlinconf.android.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The SKaiNET design system — dark-first, near-black canvas with a signature red accent,
 * mirroring the official examples (examples.skainet.sk) and the `sk.ainet.ui` theme. Kept
 * self-contained here so the KotlinConf demo needs no extra UI artifact.
 */

// Dark theme (the default) --------------------------------------------------
private val DarkBackground = Color(0xFF0A0B0D)
private val DarkForeground = Color(0xFFF2F2F2)
private val DarkSurface = Color(0xFF0F1114)
private val DarkSurfaceVariant = Color(0xFF1A1D21)
private val DarkPrimary = Color(0xFFDC2626)       // SKaiNET red
private val DarkPrimaryGlow = Color(0xFFEF4444)
private val DarkSecondary = Color(0xFF1F2328)
private val DarkSecondaryForeground = Color(0xFFD9D9D9)
private val DarkMutedForeground = Color(0xFF9AA1AC)
private val DarkBorder = Color(0xFF262B31)

// Light theme ---------------------------------------------------------------
private val LightBackground = Color(0xFFFAFAFA)
private val LightForeground = Color(0xFF0F172A)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF1F5F9)
private val LightPrimary = Color(0xFFDC2626)
private val LightSecondary = Color(0xFFF1F5F9)
private val LightSecondaryForeground = Color(0xFF1E293B)
private val LightMutedForeground = Color(0xFF64748B)
private val LightBorder = Color(0xFFE2E8F0)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkPrimaryGlow,
    onPrimaryContainer = Color.White,
    secondary = DarkSecondary,
    onSecondary = DarkSecondaryForeground,
    secondaryContainer = DarkSecondary,
    onSecondaryContainer = DarkSecondaryForeground,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkSurface,
    onSurface = DarkForeground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMutedForeground,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
)

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimary.copy(alpha = 0.1f),
    onPrimaryContainer = LightPrimary,
    secondary = LightSecondary,
    onSecondary = LightSecondaryForeground,
    secondaryContainer = LightSecondary,
    onSecondaryContainer = LightSecondaryForeground,
    background = LightBackground,
    onBackground = LightForeground,
    surface = LightSurface,
    onSurface = LightForeground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightMutedForeground,
    outline = LightBorder,
    outlineVariant = LightBorder,
)

@Composable
fun SKaiNETTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
