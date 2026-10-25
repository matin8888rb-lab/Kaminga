package com.kaminga.reader.data.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.kaminga.reader.domain.model.ImportedChapter
import com.kaminga.reader.domain.repository.ChapterImporter
import com.kaminga.reader.domain.usecase.naturalCompare
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * عکس‌های انتخاب‌شده را داخل حافظه‌ی خصوصی برنامه کپی می‌کند. این‌طوری بعد از بستن
 * برنامه هم در دسترس‌اند و به مجوز دائمی فایل‌ها (که محدودیت تعداد دارد) نیازی نیست.
 */
class FileChapterImporter(context: Context) : ChapterImporter {

    private val appContext: Context = context.applicationContext
    private val rootDir: File = File(appContext.filesDir, "imported")

    private class Source(val uri: Uri, val name: String, val mime: String)

    override suspend fun importChapter(
        comicId: String,
        sourceUris: List<String>,
        title: String
    ): Result<ImportedChapter> = withContext(Dispatchers.IO) {
        val chapterId = UUID.randomUUID().toString()
        val dir = File(rootDir, chapterId)
        try {
            Result.success(copyPages(chapterId, comicId, title, sourceUris, dir))
        } catch (e: CancellationException) {
            dir.deleteRecursively()
            throw e
        } catch (e: Exception) {
            dir.deleteRecursively()
            Result.failure(e)
        }
    }

    override suspend fun deleteFiles(pagePaths: List<String>) {
        withContext(Dispatchers.IO) {
            val parent = pagePaths.firstOrNull()?.let { File(it).parentFile } ?: return@withContext
            // فقط داخل پوشه‌ی خود برنامه پاک می‌کنیم.
            if (parent.absolutePath.startsWith(rootDir.absolutePath)) {
                parent.deleteRecursively()
            }
        }
    }

    private fun copyPages(
        chapterId: String,
        comicId: String,
        title: String,
        rawUris: List<String>,
        dir: File
    ): ImportedChapter {
        val resolver = appContext.contentResolver
        val sources = rawUris.mapNotNull { raw ->
            val uri = Uri.parse(raw)
            val mime = resolver.getType(uri)
            if (mime != null && mime.startsWith("image/")) {
                Source(uri, displayName(uri) ?: raw.substringAfterLast('/'), mime)
            } else {
                null
            }
        }
        require(sources.isNotEmpty()) { "No image files were selected." }

        val sorted = sources
            .sortedWith(Comparator { a, b -> naturalCompare(a.name, b.name) })
            .take(MAX_PAGES)

        if (!dir.mkdirs() && !dir.isDirectory) throw IOException("Cannot create folder.")

        val paths = ArrayList<String>(sorted.size)
        sorted.forEachIndexed { index, source ->
            val target = File(dir, String.format(Locale.US, "page_%04d.%s", index + 1, extensionFor(source.mime)))
            val input = resolver.openInputStream(source.uri) ?: throw IOException("Cannot open image.")
            input.use { stream -> target.outputStream().use { output -> stream.copyTo(output) } }
            if (target.length() > MAX_FILE_BYTES) {
                target.delete()
            } else {
                paths.add(target.absolutePath)
            }
        }
        require(paths.isNotEmpty()) { "No usable images." }

        return ImportedChapter(chapterId, comicId, title, paths, System.currentTimeMillis())
    }

    private fun displayName(uri: Uri): String? =
        appContext.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

    private fun extensionFor(mime: String): String = when (mime) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "jpg"
    }

    private companion object {
        const val MAX_PAGES = 300
        const val MAX_FILE_BYTES = 30L * 1024L * 1024L
    }
}
