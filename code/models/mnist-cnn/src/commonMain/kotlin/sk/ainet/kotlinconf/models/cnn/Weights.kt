package sk.ainet.kotlinconf.models.cnn

import kotlinx.io.Buffer
import sk.ainet.io.gguf.GGUFReader
import sk.ainet.lang.nn.Module
import sk.ainet.lang.tensor.data.FloatArrayTensorData
import sk.ainet.lang.types.FP32

/**
 * Common-code GGUF weight loader — works on every target (JVM, Android, …) because it
 * only touches the public `FloatArrayTensorData.buffer`, no JVM reflection.
 *
 * A GGUF file is a flat map of named tensors. We match each tensor by name to a
 * trainable parameter of [module] and copy the floats in. The layer names in
 * [mnistCnn] (`stage1.conv1`, `stage2.conv2`, `out`) are chosen to line up exactly with
 * the tensor names in `mnist_cnn.gguf` (`stage1.conv1.weight`, `…bias`, `out.weight`, …).
 */
fun loadCnnWeights(module: Module<FP32, Float>, bytes: ByteArray) {
    val reader = GGUFReader(Buffer().apply { write(bytes) })
    val tensorMap = reader.tensors.associateBy { it.name }

    module.trainableParameters().forEach { param ->
        val readerTensor = tensorMap[param.name] ?: return@forEach
        val data = param.value.data
        if (data is FloatArrayTensorData<*>) {
            val buf = data.buffer
            readerTensor.data.forEachIndexed { idx, value ->
                buf[idx] = (value as Number).toFloat()
            }
        }
    }
}
