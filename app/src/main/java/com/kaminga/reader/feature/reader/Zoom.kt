package com.kaminga.reader.feature.reader

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.IntSize

private const val MAX_SCALE = 4f

/** وضعیت بزرگ‌نمایی یک صفحه: مقیاس بین ۱ و ۴ و جابه‌جایی که از لبه‌ها بیرون نمی‌زند. */
@Stable
class ZoomState {
    var scale by mutableFloatStateOf(1f)
        private set
    var offsetX by mutableFloatStateOf(0f)
        private set
    var offsetY by mutableFloatStateOf(0f)
        private set

    val isZoomed: Boolean
        get() = scale > 1.01f

    fun transform(zoomChange: Float, pan: Offset, size: IntSize) {
        val newScale = (scale * zoomChange).coerceIn(1f, MAX_SCALE)
        val maxX = size.width * (newScale - 1f) / 2f
        val maxY = size.height * (newScale - 1f) / 2f
        scale = newScale
        offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
        offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
    }

    fun toggle() {
        if (isZoomed) reset() else scale = 2f
    }

    fun reset() {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }
}

/**
 * با دو انگشت بزرگ‌نمایی می‌کند. تا وقتی بزرگ نشده، کشیدن با یک انگشت را مصرف
 * نمی‌کند تا ورق زدن صفحه کار کند.
 */
fun Modifier.zoomGestures(zoom: ZoomState?): Modifier =
    if (zoom == null) {
        this
    } else {
        pointerInput(zoom) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                do {
                    val event = awaitPointerEvent()
                    val zoomChange = event.calculateZoom()
                    val panChange = event.calculatePan()
                    val multiTouch = event.changes.size > 1
                    if (multiTouch || zoom.isZoomed) {
                        zoom.transform(zoomChange, panChange, size)
                        event.changes.forEach { change ->
                            if (change.positionChanged()) change.consume()
                        }
                    }
                } while (event.changes.any { it.pressed })
            }
        }
    }
