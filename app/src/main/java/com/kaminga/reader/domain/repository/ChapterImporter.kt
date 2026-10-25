package com.kaminga.reader.domain.repository

import com.kaminga.reader.domain.model.ImportedChapter

interface ChapterImporter {
    /** عکس‌های انتخاب‌شده را به حافظه‌ی برنامه کپی می‌کند و یک فصل می‌سازد. */
    suspend fun importChapter(
        comicId: String,
        sourceUris: List<String>,
        title: String
    ): Result<ImportedChapter>

    suspend fun deleteFiles(pagePaths: List<String>)
}
