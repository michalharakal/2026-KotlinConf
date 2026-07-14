package sk.ainet.kotlinconf.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import sk.ainet.kotlinconf.android.theme.SKaiNETTheme

/**
 * The KotlinConf 2026 SKaiNET demo — two interactive, on-device demos (draw-a-digit CNN
 * and a live-trained transformer) running the exact same `sk.ainet.kotlinconf.*` shared
 * code the `:cli` runs on the JVM. Device-first, one codebase.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12 launch splash (backported): shows the SKaiNET logo over the dark canvas
        // instead of the OS's generic blank window. Must run before super.onCreate.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SKaiNETTheme {
                AppRoot()
            }
        }
    }
}
