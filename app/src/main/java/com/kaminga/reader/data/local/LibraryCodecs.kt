package com.kaminga.reader.data.local

import com.kaminga.reader.domain.model.ImportedChapter
import com.kaminga.reader.domain.model.ReadingProgress

/**
 * ذخیره‌ی ساده‌ی متنی برای DataStore. جداکننده‌ها نویسه‌های کنترلی‌اند که در
 * عنوان و مسیر فایل نمی‌آیند؛ اگر آمدند به فاصله تبدیل می‌شوند. رکورد خراب نادیده گرفته می‌شود.
 */
object LibraryCodecs {
    private const val RECORD = '\u001E'
    private const val FIELD = '\u001F'
    private const val PATH = '\u001D'

    private fun clean(text: String): String =
        text.replace(RECORD, ' ').replace(FIELD, ' ').replace(PATH, ' ')

    fun encodeProgress(map: Map<String, ReadingProgress>): String =
        map.values.joinToString(RECORD.toString()) { p ->
            listOf(
                clean(p.comicId), clean(p.chapterId),
                p.chapterNumber.toString(), p.pageIndex.toString(),
                p.pageCount.toString(), p.updatedAt.toString()
            ).joinToString(FIELD.toString())
        }

    fun decodeProgress(text: String?): Map<String, ReadingProgress> {
        if (text.isNullOrEmpty()) return emptyMap()
        val result = LinkedHashMap<String, ReadingProgress>()
        for (record in text.split(RECORD)) {
            val f = record.split(FIELD)
            if (f.size != 6) continue
            val chapterNumber = f[2].toIntOrNull() ?: continue
            val pageIndex = f[3].toIntOrNull() ?: continue
            val pageCount = f[4].toIntOrNull() ?: continue
            val updatedAt = f[5].toLongOrNull() ?: continue
            if (f[0].isEmpty() || f[1].isEmpty()) continue
            result[f[0]] = ReadingProgress(f[0], f[1], chapterNumber, pageIndex, pageCount, updatedAt)
        }
        return result
    }

    fun encodeImported(list: List<ImportedChapter>): String =
        list.joinToString(RECORD.toString()) { c ->
            listOf(
                clean(c.id), clean(c.comicId), clean(c.title), c.createdAt.toString(),
                c.pagePaths.joinToString(PATH.toString()) { clean(it) }
            ).joinToString(FIELD.toString())
        }

    fun decodeImported(text: String?): List<ImportedChapter> {
        if (text.isNullOrEmpty()) return emptyList()
        val result = ArrayList<ImportedChapter>()
        for (record in text.split(RECORD)) {
            val f = record.split(FIELD)
            if (f.size != 5) continue
            val createdAt = f[3].toLongOrNull() ?: continue
            if (f[0].isEmpty() || f[1].isEmpty()) continue
            val paths = if (f[4].isEmpty()) emptyList() else f[4].split(PATH)
            result.add(ImportedChapter(f[0], f[1], f[2], paths, createdAt))
        }
        return result
    }
}
