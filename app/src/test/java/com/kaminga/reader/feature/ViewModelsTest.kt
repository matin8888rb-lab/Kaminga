package com.kaminga.reader.feature

import com.kaminga.reader.core.common.UiMessages
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.domain.model.Chapter
import com.kaminga.reader.domain.model.ComicType
import com.kaminga.reader.domain.model.ReadingProgress
import com.kaminga.reader.domain.model.SortOrder
import com.kaminga.reader.feature.details.DetailsViewModel
import com.kaminga.reader.feature.home.HOME_SECTION_LIMIT
import com.kaminga.reader.feature.home.HomeViewModel
import com.kaminga.reader.feature.home.buildHomeContent
import com.kaminga.reader.feature.library.LibraryViewModel
import com.kaminga.reader.feature.reader.ReaderViewModel
import com.kaminga.reader.feature.search.SearchViewModel
import com.kaminga.reader.testutil.FakeChapterImporter
import com.kaminga.reader.testutil.FakeComicRepository
import com.kaminga.reader.testutil.InMemoryUserLibraryRepository
import com.kaminga.reader.testutil.MainDispatcherRule
import com.kaminga.reader.testutil.sampleComic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val comics = listOf(
        sampleComic("a", title = "الف", originalTitle = "ALPHA", popularity = 10, addedOrder = 3, isFeatured = true)
            .let { comic ->
                comic.copy(
                    chapters = listOf(
                        Chapter("a-1", 1, "یک", pageCount = 5),
                        Chapter("a-2", 2, "دو", pageCount = 5)
                    )
                )
            },
        sampleComic("b", title = "ب", originalTitle = "BRAVO", type = ComicType.MANGA, popularity = 30, addedOrder = 1),
        sampleComic("c", title = "پ", originalTitle = "CHARLIE", popularity = 20, addedOrder = 2)
    )

    private fun progress(comicId: String, chapterId: String, number: Int, page: Int, at: Long = 1L) =
        ReadingProgress(comicId, chapterId, number, page, 5, at)

    // --- Home ---

    @Test
    fun homeContentIsBuiltFromRepository() {
        val vm = HomeViewModel(FakeComicRepository(comics), InMemoryUserLibraryRepository())
        val state = vm.state.value
        assertTrue(state is UiState.Success)
        val content = (state as UiState.Success).data
        assertEquals(listOf("a"), content.featured.map { it.id })
        assertEquals(listOf("a", "c", "b"), content.newest.map { it.id })
        assertEquals(listOf("b", "c", "a"), content.popular.map { it.id })
        assertTrue(content.continueReading.isEmpty())
    }

    @Test
    fun homeShowsContinueReadingFromSavedProgress() {
        val library = InMemoryUserLibraryRepository()
        library.seedProgress(progress("c", "c-1", 1, 2, at = 100L))
        library.seedProgress(progress("a", "a-2", 2, 4, at = 200L))
        val vm = HomeViewModel(FakeComicRepository(comics), library)
        val content = (vm.state.value as UiState.Success).data
        assertEquals(listOf("a", "c"), content.continueReading.map { it.comic.id })
    }

    @Test
    fun homeSectionsAreLimited() {
        val many = (1..20).map { sampleComic("id$it", popularity = it, addedOrder = it) }
        val content = buildHomeContent(many)
        assertEquals(HOME_SECTION_LIMIT, content.newest.size)
        assertEquals(HOME_SECTION_LIMIT, content.popular.size)
        assertEquals("id20", content.newest.first().id)
    }

    @Test
    fun homeShowsErrorThenRecoversOnRetry() {
        val repo = FakeComicRepository(comics, failure = RuntimeException("offline"))
        val vm = HomeViewModel(repo, InMemoryUserLibraryRepository())
        val error = vm.state.value
        assertTrue(error is UiState.Error)
        assertEquals(UiMessages.LOAD_FAILED, (error as UiState.Error).message)

        repo.failure = null
        vm.load()
        assertTrue(vm.state.value is UiState.Success)
    }

    @Test
    fun emptyRepositoryGivesEmptyHomeContent() {
        val vm = HomeViewModel(FakeComicRepository(emptyList()), InMemoryUserLibraryRepository())
        val content = (vm.state.value as UiState.Success).data
        assertTrue(content.isEmpty)
    }

    // --- Search ---

    @Test
    fun searchStartsWithAllComicsSortedByPopularity() {
        val vm = SearchViewModel(FakeComicRepository(comics))
        val result = (vm.state.value as UiState.Success).data
        assertEquals(listOf("b", "c", "a"), result.map { it.id })
    }

    @Test
    fun searchQueryAndFiltersUpdateResults() {
        val vm = SearchViewModel(FakeComicRepository(comics))

        vm.updateFilters { it.copy(query = "bravo") }
        assertEquals(listOf("b"), (vm.state.value as UiState.Success).data.map { it.id })

        vm.updateFilters { it.copy(query = "", type = ComicType.COMIC) }
        assertEquals(listOf("c", "a"), (vm.state.value as UiState.Success).data.map { it.id })

        vm.updateFilters { it.copy(sort = SortOrder.NEWEST) }
        assertEquals(listOf("a", "c"), (vm.state.value as UiState.Success).data.map { it.id })
    }

    @Test
    fun clearFiltersResetsEverythingButSort() {
        val vm = SearchViewModel(FakeComicRepository(comics))
        vm.updateFilters { it.copy(query = "zzz", type = ComicType.MANGA, sort = SortOrder.NEWEST) }
        assertTrue((vm.state.value as UiState.Success).data.isEmpty())

        vm.clearFilters()
        assertFalse(vm.filters.value.hasActiveFilters)
        assertEquals(SortOrder.NEWEST, vm.filters.value.sort)
        assertEquals(3, (vm.state.value as UiState.Success).data.size)
    }

    @Test
    fun searchShowsErrorWhenLoadFails() {
        val vm = SearchViewModel(FakeComicRepository(comics, failure = RuntimeException("offline")))
        assertTrue(vm.state.value is UiState.Error)
    }

    // --- Details ---

    private fun detailsVm(
        library: InMemoryUserLibraryRepository = InMemoryUserLibraryRepository(),
        importer: FakeChapterImporter = FakeChapterImporter(),
        repo: FakeComicRepository = FakeComicRepository(comics),
        id: String = "a"
    ) = DetailsViewModel(repo, library, importer, id)

    @Test
    fun detailsLoadsExistingComic() {
        val vm = detailsVm(id = "b")
        val content = (vm.state.value as UiState.Success).data
        assertEquals("BRAVO", content.comic.originalTitle)
        assertFalse(content.isFavorite)
        assertNull(content.progress)
    }

    @Test
    fun detailsReportsNotFoundWithoutRetry() {
        val vm = detailsVm(id = "missing")
        val error = vm.state.value as UiState.Error
        assertEquals(UiMessages.NOT_FOUND, error.message)
        assertFalse(error.retryable)
    }

    @Test
    fun detailsReportsRetryableErrorOnFailure() {
        val vm = detailsVm(repo = FakeComicRepository(comics, failure = RuntimeException("offline")))
        val error = vm.state.value as UiState.Error
        assertEquals(UiMessages.LOAD_FAILED, error.message)
        assertTrue(error.retryable)
    }

    @Test
    fun detailsTogglesFavorite() {
        val library = InMemoryUserLibraryRepository()
        val vm = detailsVm(library = library)
        assertFalse((vm.state.value as UiState.Success).data.isFavorite)

        vm.toggleFavorite()
        assertTrue((vm.state.value as UiState.Success).data.isFavorite)

        vm.toggleFavorite()
        assertFalse((vm.state.value as UiState.Success).data.isFavorite)
    }

    @Test
    fun detailsOffersContinueOnlyForExistingReadableChapter() {
        val library = InMemoryUserLibraryRepository()
        library.seedProgress(progress("a", "a-2", 2, 3))
        val content = (detailsVm(library = library).state.value as UiState.Success).data
        assertEquals("a-2", content.continueChapter?.id)
        assertEquals("a-1", content.firstReadableChapter?.id)

        val stale = InMemoryUserLibraryRepository()
        stale.seedProgress(progress("a", "deleted-chapter", 9, 1))
        val staleContent = (detailsVm(library = stale).state.value as UiState.Success).data
        assertNull(staleContent.continueChapter)
    }

    @Test
    fun detailsImportAddsPersonalChapterAfterExistingOnes() {
        val library = InMemoryUserLibraryRepository()
        val vm = detailsVm(library = library)

        vm.importChapter(listOf("p1", "p2", "p3"))

        assertEquals(UiMessages.IMPORT_DONE, vm.importState.value.message)
        assertFalse(vm.importState.value.isImporting)
        val chapters = (vm.state.value as UiState.Success).data.comic.chapters
        assertEquals(3, chapters.size)
        val personal = chapters.last()
        assertTrue(personal.isPersonal)
        assertEquals(3, personal.number)
        assertEquals(3, personal.pageCount)
        assertEquals(1, library.currentImported.size)
    }

    @Test
    fun detailsImportFailureShowsMessageAndAddsNothing() {
        val library = InMemoryUserLibraryRepository()
        val vm = detailsVm(library = library, importer = FakeChapterImporter(failure = RuntimeException("disk full")))

        vm.importChapter(listOf("p1"))

        assertEquals(UiMessages.IMPORT_FAILED, vm.importState.value.message)
        assertTrue(library.currentImported.isEmpty())
        assertEquals(2, (vm.state.value as UiState.Success).data.comic.chapters.size)
    }

    @Test
    fun detailsDeletesPersonalChapterAndItsFiles() {
        val library = InMemoryUserLibraryRepository()
        val importer = FakeChapterImporter()
        val vm = detailsVm(library = library, importer = importer)
        vm.importChapter(listOf("p1", "p2"))
        val personal = (vm.state.value as UiState.Success).data.comic.chapters.last()

        vm.deletePersonalChapter(personal)

        assertEquals(listOf(listOf("p1", "p2")), importer.deleted)
        assertEquals(2, (vm.state.value as UiState.Success).data.comic.chapters.size)
    }

    @Test
    fun detailsDoesNotDeleteBuiltInChapters() {
        val importer = FakeChapterImporter()
        val vm = detailsVm(importer = importer)
        val builtIn = (vm.state.value as UiState.Success).data.comic.chapters.first()
        vm.deletePersonalChapter(builtIn)
        assertTrue(importer.deleted.isEmpty())
    }

    // --- Reader ---

    private fun readerVm(
        library: InMemoryUserLibraryRepository = InMemoryUserLibraryRepository(),
        comicId: String = "a",
        chapterId: String = "a-2",
        repo: FakeComicRepository = FakeComicRepository(comics)
    ) = ReaderViewModel(repo, library, comicId, chapterId, clock = { 777L })

    @Test
    fun readerStartsAtFirstPageWithoutProgress() {
        val content = (readerVm().state.value as UiState.Success).data
        assertEquals("a-2", content.chapter.id)
        assertEquals(0, content.initialPage)
        assertEquals("a-1", content.previousChapterId)
        assertNull(content.nextChapterId)
    }

    @Test
    fun readerResumesFromSavedPageOfSameChapter() {
        val library = InMemoryUserLibraryRepository()
        library.seedProgress(progress("a", "a-2", 2, 3))
        val content = (readerVm(library = library).state.value as UiState.Success).data
        assertEquals(3, content.initialPage)
    }

    @Test
    fun readerIgnoresProgressOfAnotherChapterAndClampsBadPage() {
        val otherChapter = InMemoryUserLibraryRepository()
        otherChapter.seedProgress(progress("a", "a-1", 1, 4))
        assertEquals(0, (readerVm(library = otherChapter).state.value as UiState.Success).data.initialPage)

        val outOfRange = InMemoryUserLibraryRepository()
        outOfRange.seedProgress(progress("a", "a-2", 2, 99))
        assertEquals(4, (readerVm(library = outOfRange).state.value as UiState.Success).data.initialPage)
    }

    @Test
    fun readerSavesProgressWhenPageChanges() {
        val library = InMemoryUserLibraryRepository()
        val vm = readerVm(library = library)

        vm.onPageChanged(2)

        val saved = library.currentProgress["a"]
        assertNotNull(saved)
        assertEquals("a-2", saved!!.chapterId)
        assertEquals(2, saved.chapterNumber)
        assertEquals(2, saved.pageIndex)
        assertEquals(5, saved.pageCount)
        assertEquals(777L, saved.updatedAt)
    }

    @Test
    fun readerClampsPageBeforeSaving() {
        val library = InMemoryUserLibraryRepository()
        readerVm(library = library).onPageChanged(50)
        assertEquals(4, library.currentProgress["a"]!!.pageIndex)
    }

    @Test
    fun readerReportsMissingChapterAndMissingComic() {
        val missingChapter = readerVm(chapterId = "nope").state.value as UiState.Error
        assertEquals(UiMessages.CHAPTER_NOT_FOUND, missingChapter.message)
        assertFalse(missingChapter.retryable)

        val missingComic = readerVm(comicId = "nope").state.value as UiState.Error
        assertEquals(UiMessages.NOT_FOUND, missingComic.message)
    }

    @Test
    fun readerFindsPersonalChapterAndSkipsUnreadableNeighbours() {
        val withEmpty = comics.map { comic ->
            if (comic.id == "a") {
                comic.copy(
                    chapters = listOf(
                        Chapter("a-1", 1, "یک", pageCount = 5),
                        Chapter("a-2", 2, "خالی", pageCount = 0),
                        Chapter("a-3", 3, "سه", pageCount = 5)
                    )
                )
            } else {
                comic
            }
        }
        val first = (readerVm(chapterId = "a-1", repo = FakeComicRepository(withEmpty)).state.value as UiState.Success).data
        assertEquals("a-3", first.nextChapterId)

        val library = InMemoryUserLibraryRepository()
        val vm = detailsVm(library = library)
        vm.importChapter(listOf("p1", "p2"))
        val personalId = library.currentImported.first().id
        val personal = (readerVm(library = library, chapterId = personalId).state.value as UiState.Success).data
        assertTrue(personal.chapter.isPersonal)
        assertEquals("a-2", personal.previousChapterId)
        assertEquals(2, personal.pageCount)
    }

    // --- Library ---

    @Test
    fun libraryListsFavoritesAndHistory() {
        val library = InMemoryUserLibraryRepository()
        library.seedFavorite("b")
        library.seedFavorite("zzz")
        library.seedProgress(progress("a", "a-1", 1, 0, at = 5L))
        val vm = LibraryViewModel(FakeComicRepository(comics), library)

        val content = (vm.state.value as UiState.Success).data
        assertEquals(listOf("b"), content.favorites.map { it.id })
        assertEquals(listOf("a"), content.history.map { it.comic.id })
    }

    @Test
    fun libraryShowsErrorWhenLoadFails() {
        val vm = LibraryViewModel(
            FakeComicRepository(comics, failure = RuntimeException("offline")),
            InMemoryUserLibraryRepository()
        )
        assertTrue(vm.state.value is UiState.Error)
    }
}
