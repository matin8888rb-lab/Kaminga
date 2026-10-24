package com.kaminga.reader.feature

import com.kaminga.reader.core.common.UiMessages
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.domain.model.ComicType
import com.kaminga.reader.domain.model.SortOrder
import com.kaminga.reader.feature.details.DetailsViewModel
import com.kaminga.reader.feature.home.HOME_SECTION_LIMIT
import com.kaminga.reader.feature.home.HomeViewModel
import com.kaminga.reader.feature.home.buildHomeContent
import com.kaminga.reader.feature.search.SearchViewModel
import com.kaminga.reader.testutil.FakeComicRepository
import com.kaminga.reader.testutil.MainDispatcherRule
import com.kaminga.reader.testutil.sampleComic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val comics = listOf(
        sampleComic("a", title = "الف", originalTitle = "ALPHA", popularity = 10, addedOrder = 3, isFeatured = true),
        sampleComic("b", title = "ب", originalTitle = "BRAVO", type = ComicType.MANGA, popularity = 30, addedOrder = 1),
        sampleComic("c", title = "پ", originalTitle = "CHARLIE", popularity = 20, addedOrder = 2)
    )

    // --- Home ---

    @Test
    fun homeContentIsBuiltFromRepository() {
        val vm = HomeViewModel(FakeComicRepository(comics))
        val state = vm.state.value
        assertTrue(state is UiState.Success)
        val content = (state as UiState.Success).data
        assertEquals(listOf("a"), content.featured.map { it.id })
        assertEquals(listOf("a", "c", "b"), content.newest.map { it.id })
        assertEquals(listOf("b", "c", "a"), content.popular.map { it.id })
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
        val vm = HomeViewModel(repo)
        val error = vm.state.value
        assertTrue(error is UiState.Error)
        assertEquals(UiMessages.LOAD_FAILED, (error as UiState.Error).message)

        repo.failure = null
        vm.load()
        assertTrue(vm.state.value is UiState.Success)
    }

    @Test
    fun emptyRepositoryGivesEmptyHomeContent() {
        val vm = HomeViewModel(FakeComicRepository(emptyList()))
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

    @Test
    fun detailsLoadsExistingComic() {
        val vm = DetailsViewModel(FakeComicRepository(comics), "b")
        val comic = (vm.state.value as UiState.Success).data
        assertEquals("BRAVO", comic.originalTitle)
    }

    @Test
    fun detailsReportsNotFoundWithoutRetry() {
        val vm = DetailsViewModel(FakeComicRepository(comics), "missing")
        val error = vm.state.value as UiState.Error
        assertEquals(UiMessages.NOT_FOUND, error.message)
        assertFalse(error.retryable)
    }

    @Test
    fun detailsReportsRetryableErrorOnFailure() {
        val vm = DetailsViewModel(FakeComicRepository(comics, failure = RuntimeException("offline")), "a")
        val error = vm.state.value as UiState.Error
        assertEquals(UiMessages.LOAD_FAILED, error.message)
        assertTrue(error.retryable)
    }
}
