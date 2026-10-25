package com.kaminga.reader.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.ui.theme.Cyan
import com.kaminga.reader.ui.theme.Muted
import com.kaminga.reader.ui.theme.Night

// هر پنل: چپ، بالا، راست، پایین (کسری از اندازه‌ی صفحه)
private val panelLayouts: List<List<FloatArray>> = listOf(
    listOf(
        floatArrayOf(0f, 0f, 1f, 0.55f),
        floatArrayOf(0f, 0.58f, 0.49f, 1f),
        floatArrayOf(0.51f, 0.58f, 1f, 1f)
    ),
    listOf(
        floatArrayOf(0f, 0f, 1f, 0.3f),
        floatArrayOf(0f, 0.33f, 1f, 0.62f),
        floatArrayOf(0f, 0.65f, 1f, 1f)
    ),
    listOf(
        floatArrayOf(0f, 0f, 0.49f, 0.49f),
        floatArrayOf(0.51f, 0f, 1f, 0.49f),
        floatArrayOf(0f, 0.51f, 0.49f, 1f),
        floatArrayOf(0.51f, 0.51f, 1f, 1f)
    )
)

/**
 * صفحه‌ی نمونه‌ی ساختگی که داخل خود برنامه کشیده می‌شود (بدون هیچ تصویر بیرونی).
 * `pageIndex` از صفر شروع می‌شود. نسبت پیشنهادی صفحه ۲ به ۳ است.
 */
@Composable
fun DemoPage(
    comic: Comic,
    chapterNumber: Int,
    pageIndex: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    val accent = Color(comic.accentColor)
    val layout = panelLayouts[pageIndex % panelLayouts.size]

    Box(
        modifier.background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.6f), Night)))
    ) {
        Canvas(Modifier.fillMaxSize().padding(14.dp)) {
            layout.forEach { f ->
                val topLeft = Offset(size.width * f[0], size.height * f[1])
                val panelSize = Size(size.width * (f[2] - f[0]), size.height * (f[3] - f[1]))
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.07f),
                    topLeft = topLeft,
                    size = panelSize,
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = accent.copy(alpha = 0.95f),
                    topLeft = topLeft,
                    size = panelSize,
                    cornerRadius = CornerRadius(14f, 14f),
                    style = Stroke(width = 3f)
                )
            }
        }
        Text(
            "${comic.originalTitle} • CH $chapterNumber",
            color = Cyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart).padding(22.dp)
        )
        Text(
            "${pageIndex + 1}",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.Center)
        )
        Text(
            "صفحه‌ی نمونه ${pageIndex + 1} از $pageCount • DEMO",
            color = Muted,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(22.dp)
        )
    }
}
