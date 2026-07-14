package sk.ainet.kotlinconf

import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.kotlinconf.tensors.batch
import sk.ainet.kotlinconf.tensors.matrix
import sk.ainet.kotlinconf.tensors.numpySlice
import sk.ainet.kotlinconf.tensors.scalar
import sk.ainet.kotlinconf.tensors.vector
import sk.ainet.kotlinconf.linear.forwardNet
import sk.ainet.kotlinconf.linear.linearLayer
import sk.ainet.kotlinconf.linear.runForward
import kotlin.test.Test
import kotlin.test.assertEquals

/** Shape assertions for the foundational `tensors` and `linear` demos that live in :shared. */
class FoundationsTest {

    private val ctx = DirectCpuExecutionContext.create()

    @Test
    fun tensors_shapes() {
        assertEquals(listOf(1), scalar(ctx).shape.dimensions.toList())
        assertEquals(listOf(4), vector(ctx).shape.dimensions.toList())
        assertEquals(listOf(2, 3), matrix(ctx).shape.dimensions.toList())
        assertEquals(listOf(8, 3, 2, 2), batch(ctx).shape.dimensions.toList())
        // numpy t[0:2, 1, :, 0:6:2] on a [4,3,2,6] tensor -> [2, 2, 3]
        assertEquals(listOf(2, 2, 3), numpySlice(ctx).shape.dimensions.toList())
    }

    @Test
    fun linear_forwardShapes() {
        assertEquals(listOf(1, 2), runForward(ctx, linearLayer()).shape.dimensions.toList())
        assertEquals(listOf(1, 1), runForward(ctx, forwardNet()).shape.dimensions.toList())
    }
}
