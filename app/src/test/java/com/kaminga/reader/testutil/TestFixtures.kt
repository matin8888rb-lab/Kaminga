package com.kaminga.reader.testutil

import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.ComicLanguage
import com.kaminga.reader.domain.model.ComicType
import com.kaminga.reader.domain.model.Genre
import com.kaminga.reader.domain.model.ImportedChapter
import com.kaminga.reader.domain.model.PublicationStatus
import com.kaminga.reader.domain.model.ReadingProgress
import com.kaminga.reader.domain.repository.ChapterImporter
import com.kaminga.reader.domain.repository.UserLibraryRepository
import com.kaminga.reader.domain.repository.ComicNotFoundException
import com.kaminga.reader.domain.repository.ComicRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

fun sampleComic(
    id: String = "c1",
    title: String = "عنوان",
    originalTitle: String = "TITLE",
    type: ComicType = ComicType.COMIC,
    genres: List<Genre> = listOf(Genre.ACTION),
    author: String = "نویسنده",
    artist: String = "هنرمند",
    language: ComicLanguage = ComicLanguage.FA,
    status: PublicationStatus = PublicationStatus.ONGOING,
    popularity: Int = 0,
    addedOrder: Int = 0,
    isFeatured: Boolean = false
): Comic = Comic(
    id = id,
    title = title,
    originalTitle = originalTitle,
    summary = "خلاصه",
    type = type,
    genres = genres,
    author = author,
    artist = artist,
    language = language,
    status = status,
    popularity = popularity,
    addedOrder = addedOrder,
    isFeatured = isFeatured
)

/** مخزن آزمایشی که می‌تواند موفق یا ناموفق باشد. */
class FakeComicRepository(
    var comics: List<Comic> = emptyList(),
    var failure: Throwable? = null
) : ComicRepository {

    override suspend fun getComics(): Result<List<Comic>> {
        val error = failure
        return if (error != null) Result.failure(error) else Result.success(comics)
    }

    override suspend fun getComic(id: String): Result<Comic> {
        val error = failure
        if (error != null) return Result.failure(error)
        val comic = comics.firstOrNull { it.id == id }
            ?: return Result.failure(ComicNotFoundException(id))
        return Result.success(comic)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/** مخزن شخصی در حافظه برای تست؛ seed* برای آماده کردن داده بدون coroutine است. */
class InMemoryUserLibraryRepository : UserLibraryRepository {
    private val favorites = MutableStateFlow<Set<String>>(emptySet())
    private val progressMap = MutableStateFlow<Map<String, ReadingProgress>>(emptyMap())
    private val imported = MutableStateFlow<List<ImportedChapter>>(emptyList())

    override val favoriteIds: Flow<Set<String>> = favorites
    override val progress: Flow<Map<String, ReadingProgress>> = progressMap
    override val importedChapters: Flow<List<ImportedChapter>> = imported

    val currentProgress: Map<String, ReadingProgress>
        get() = progressMap.value
    val currentImported: List<ImportedChapter>
        get() = imported.value

    fun seedFavorite(comicId: String) {
        favorites.value = favorites.value + comicId
    }

    fun seedProgress(value: ReadingProgress) {
        progressMap.value = progressMap.value + (value.comicId to value)
    }

    override suspend fun setFavorite(comicId: String, favorite: Boolean) {
        favorites.update { current -> if (favorite) current + comicId else current - comicId }
    }

    override suspend fun saveProgress(progress: ReadingProgress) {
        progressMap.update { current -> current + (progress.comicId to progress) }
    }

    override suspend fun addImportedChapter(chapter: ImportedChapter) {
        imported.update { current -> current + chapter }
    }

    override suspend fun removeImportedChapter(chapterId: String) {
        imported.update { current -> current.filterNot { it.id == chapterId } }
    }
}

class FakeChapterImporter(var failure: Throwable? = null) : ChapterImporter {
    val deleted = mutableListOf<List<String>>()

    override suspend fun importChapter(
        comicId: String,
        sourceUris: List<String>,
        title: String
    ): Result<ImportedChapter> {
        val error = failure
        return if (error != null) {
            Result.failure(error)
        } else {
            Result.success(ImportedChapter("imp-${sourceUris.size}", comicId, title, sourceUris, 100L))
        }
    }

    override suspend fun deleteFiles(pagePaths: List<String>) {
        deleted.add(pagePaths)
    }
}
