package com.kaminga.reader.domain.usecase

import com.kaminga.reader.domain.model.Chapter
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.ContinueItem
import com.kaminga.reader.domain.model.ImportedChapter
import com.kaminga.reader.domain.model.ReadingProgress

/** فصل‌های شخصی را بعد از آخرین فصل اثر اضافه می‌کند (به ترتیب زمان ساخت). */
fun Comic.withImportedChapters(imported: List<ImportedChapter>): Comic {
    val mine = imported.filter { it.comicId == id }.sortedBy { it.createdAt }
    if (mine.isEmpty()) return this
    var nextNumber = (chapters.maxOfOrNull { it.number } ?: 0) + 1
    val extra = mine.map { item ->
        Chapter(
            id = item.id,
            number = nextNumber++,
            title = item.title,
            pageCount = item.pagePaths.size,
            pagePaths = item.pagePaths,
            isPersonal = true
        )
    }
    return copy(chapters = chapters + extra)
}

/** آثاری که پیشرفت مطالعه دارند، از تازه‌ترین به قدیمی‌ترین. */
fun buildContinueList(comics: List<Comic>, progress: Map<String, ReadingProgress>): List<ContinueItem> =
    progress.values
        .sortedByDescending { it.updatedAt }
        .mapNotNull { item ->
            val comic = comics.firstOrNull { it.id == item.comicId }
            if (comic != null) ContinueItem(comic, item) else null
        }

/** متن کوتاه «فصل N • صفحه P از T» برای نمایش پیشرفت. */
fun ReadingProgress.describe(): String =
    "فصل $chapterNumber • صفحه ${pageIndex + 1} از $pageCount"
