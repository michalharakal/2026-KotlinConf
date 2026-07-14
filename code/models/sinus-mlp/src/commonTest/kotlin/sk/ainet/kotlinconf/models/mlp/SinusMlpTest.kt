package sk.ainet.kotlinconf.models.mlp

import sk.ainet.context.DirectCpuExecutionContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertTrue

class SinusMlpTest {

    private val ctx = DirectCpuExecutionContext.create()

    @Test
    fun approximatesSine() {
        val mlp = SinusMlp(ctx)
        var maxErr = 0f
        var x = 0f
        while (x <= (PI / 2).toFloat()) {
            val err = abs(sin(x.toDouble()).toFloat() - mlp.predict(x))
            maxErr = maxOf(maxErr, err)
            x += 0.1f
        }
        // Pretrained net should track sin(x) closely across [0, π/2].
        assertTrue(maxErr < 0.05f, "max abs error $maxErr exceeded tolerance")
    }
}
