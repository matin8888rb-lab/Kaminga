package com.kaminga.reader.domain.model

/** آخرین جای مطالعه‌ی یک اثر. pageIndex از صفر شروع می‌شود. */
data class ReadingProgress(
    val comicId: String,
    val chapterId: String,
    val chapterNumber: Int,
    val pageIndex: Int,
    val pageCount: Int,
    val updatedAt: Long
)

/** فصلی که کاربر از عکس‌های گوشی ساخته؛ pagePaths مسیر فایل‌های کپی‌شده است. */
data class ImportedChapter(
    val id: String,
    val comicId: String,
    val title: String,
    val pagePaths: List<String>,
    val createdAt: Long
)

data class ContinueItem(
    val comic: Comic,
    val progress: ReadingProgress
)
