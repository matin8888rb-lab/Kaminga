package com.kaminga.reader.testutil

import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.ComicLanguage
import com.kaminga.reader.domain.model.ComicType
import com.kaminga.reader.domain.model.Genre
import com.kaminga.reader.domain.model.PublicationStatus
import com.kaminga.reader.domain.repository.ComicNotFoundException
import com.kaminga.reader.domain.repository.ComicRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
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
