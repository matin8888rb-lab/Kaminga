package com.kaminga.reader.feature

import com.kaminga.reader.feature.reader.ZoomState
import com.kaminga.reader.feature.reader.sampleSizeFor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageImageTest {

    @Test
    fun sampleSizeGrowsInPowersOfTwoWithoutGoingBelowTarget() {
        assertEquals(1, sampleSizeFor(1000, 1080))
        assertEquals(2, sampleSizeFor(4000, 1080))
        assertEquals(4, sampleSizeFor(8000, 1080))
        assertEquals(1, sampleSizeFor(0, 100))
        assertEquals(1, sampleSizeFor(100, 0))
    }

    @Test
    fun zoomStateClampsScaleAndOffset() {
        val zoom = ZoomState()
        assertFalse(zoom.isZoomed)

        zoom.transform(10f, Offset(5000f, -5000f), IntSize(1000, 2000))
        assertEquals(4f, zoom.scale, 0.001f)
        assertEquals(1500f, zoom.offsetX, 0.001f)
        assertEquals(-3000f, zoom.offsetY, 0.001f)
        assertTrue(zoom.isZoomed)

        zoom.transform(0.01f, Offset.Zero, IntSize(1000, 2000))
        assertEquals(1f, zoom.scale, 0.001f)
        assertEquals(0f, zoom.offsetX, 0.001f)
        assertEquals(0f, zoom.offsetY, 0.001f)
    }

    @Test
    fun doubleTapTogglesZoom() {
        val zoom = ZoomState()
        zoom.toggle()
        assertEquals(2f, zoom.scale, 0.001f)
        zoom.toggle()
        assertEquals(1f, zoom.scale, 0.001f)
    }
}
