package com.kaminga.reader.domain.repository

import com.kaminga.reader.domain.model.ImportedChapter
import com.kaminga.reader.domain.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

/** داده‌های شخصی کاربر روی همین گوشی: علاقه‌مندی‌ها، پیشرفت مطالعه و فصل‌های شخصی. */
interface UserLibraryRepository {
    val favoriteIds: Flow<Set<String>>

    /** کلید نقشه شناسه‌ی اثر است؛ برای هر اثر فقط آخرین جای مطالعه نگه داشته می‌شود. */
    val progress: Flow<Map<String, ReadingProgress>>
    val importedChapters: Flow<List<ImportedChapter>>

    suspend fun setFavorite(comicId: String, favorite: Boolean)
    suspend fun saveProgress(progress: ReadingProgress)
    suspend fun addImportedChapter(chapter: ImportedChapter)
    suspend fun removeImportedChapter(chapterId: String)
}
