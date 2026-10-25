package com.kaminga.reader.feature.reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import com.kaminga.reader.ui.theme.Cyan
import com.kaminga.reader.ui.theme.Muted
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** کوچک‌ترین نمونه‌برداری (توان ۲) که عرض تصویر از عرض هدف کمتر نشود. */
fun sampleSizeFor(sourceWidth: Int, targetWidth: Int): Int {
    if (sourceWidth <= 0 || targetWidth <= 0) return 1
    var sample = 1
    while (sourceWidth / (sample * 2) >= targetWidth) sample *= 2
    return sample
}

/** تصویرها را با اندازه‌ی مناسب صفحه‌ی نمایش دیکد می‌کند و چند صفحه‌ی اخیر را نگه می‌دارد. */
internal object PageBitmapLoader {
    private val cache = object : LruCache<String, Bitmap>(cacheBytes()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    private fun cacheBytes(): Int =
        (Runtime.getRuntime().maxMemory() / 8L).toInt().coerceAtLeast(4 * 1024 * 1024)

    fun load(path: String, targetWidthPx: Int): Bitmap? {
        val key = "$path@$targetWidthPx"
        val cached = cache.get(key)
        if (cached != null) return cached
        return try {
            if (!File(path).exists()) return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, targetWidthPx)
            }
            val bitmap = BitmapFactory.decodeFile(path, options) ?: return null
            cache.put(key, bitmap)
            bitmap
        } catch (e: OutOfMemoryError) {
            cache.evictAll()
            null
        } catch (e: Exception) {
            null
        }
    }
}

private sealed interface PageLoad {
    data object Loading : PageLoad
    data class Loaded(val image: ImageBitmap) : PageLoad
    data object Failed : PageLoad
}

@Composable
fun FileImagePage(
    path: String,
    targetWidthPx: Int,
    contentScale: ContentScale,
    description: String,
    modifier: Modifier = Modifier
) {
    var attempt by remember { mutableStateOf(0) }
    val load by produceState<PageLoad>(PageLoad.Loading, path, targetWidthPx, attempt) {
        value = PageLoad.Loading
        val bitmap = withContext(Dispatchers.IO) { PageBitmapLoader.load(path, targetWidthPx) }
        value = if (bitmap != null) PageLoad.Loaded(bitmap.asImageBitmap()) else PageLoad.Failed
    }

    when (val current = load) {
        is PageLoad.Loaded -> Image(
            bitmap = current.image,
            contentDescription = description,
            contentScale = contentScale,
            modifier = modifier
        )
        is PageLoad.Loading -> Box(modifier.aspectRatio(2f / 3f), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Cyan)
        }
        is PageLoad.Failed -> Box(modifier.aspectRatio(2f / 3f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("تصویر بارگذاری نشد", color = Muted, fontSize = 13.sp)
                TextButton(onClick = { attempt++ }) { Text("تلاش دوباره") }
            }
        }
    }
}
