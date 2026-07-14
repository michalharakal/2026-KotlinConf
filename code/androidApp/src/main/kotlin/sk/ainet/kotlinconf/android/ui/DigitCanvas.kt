package sk.ainet.kotlinconf.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.max

private const val STROKE = 26f

/** Holds the drawn strokes and a version counter so mutations trigger recomposition. */
class DrawState {
    val strokes = mutableStateListOf<MutableList<Offset>>()
    var version by mutableIntStateOf(0)
        private set

    fun start(at: Offset) { strokes.add(mutableListOf(at)); version++ }
    fun extend(to: Offset) { strokes.lastOrNull()?.add(to); version++ }
    fun clear() { strokes.clear(); version++ }
    val isEmpty: Boolean get() = strokes.isEmpty()
}

@Composable
fun rememberDrawState(): DrawState = remember { DrawState() }

/** A square, white drawing surface the user scribbles a digit onto with a finger. */
@Composable
fun DigitCanvas(state: DrawState, size: Dp = 280.dp, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { state.start(it) },
                    onDrag = { change, _ -> state.extend(change.position) },
                )
            },
    ) {
        state.version // read so the canvas repaints as strokes grow
        for (points in state.strokes) {
            if (points.isEmpty()) continue
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
            }
            drawPath(path, Color.Black, style = Stroke(STROKE))
        }
    }
}

/**
 * Rasterize the strokes to a [sizePx]² bitmap, then down-sample to a 28×28 MNIST frame:
 * 784 floats, row-major (row = y), 1f where there is ink, 0f elsewhere — matching the
 * white-digit-on-black-background convention the model was trained on.
 */
fun DrawState.toMnistPixels(sizePx: Int): FloatArray {
    val bitmap = ImageBitmap(sizePx, sizePx, ImageBitmapConfig.Argb8888)
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
    val paint = Paint().apply {
        color = Color.Black
        style = PaintingStyle.Stroke
        strokeWidth = STROKE
    }
    for (points in strokes) {
        if (points.isEmpty()) continue
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        }
        canvas.drawPath(path, paint)
    }

    val pixelMap = bitmap.toPixelMap()
    val zoom = ceil(max(sizePx, sizePx) / 28f).toInt().coerceAtLeast(1)
    val out = FloatArray(28 * 28)
    for (y in 0 until 28) {
        for (x in 0 until 28) {
            var ink = 0f
            for (dy in 0 until zoom) for (dx in 0 until zoom) {
                val sx = x * zoom + dx
                val sy = y * zoom + dy
                if (sx < sizePx && sy < sizePx && pixelMap[sx, sy].toArgb() != 0) ink = 1f
            }
            out[y * 28 + x] = ink
        }
    }
    return out
}
