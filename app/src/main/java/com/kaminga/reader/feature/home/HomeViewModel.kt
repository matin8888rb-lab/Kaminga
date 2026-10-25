package com.kaminga.reader.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaminga.reader.core.common.UiMessages
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.ContinueItem
import com.kaminga.reader.domain.model.ReadingProgress
import com.kaminga.reader.domain.repository.ComicRepository
import com.kaminga.reader.domain.repository.UserLibraryRepository
import com.kaminga.reader.domain.usecase.buildContinueList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val HOME_SECTION_LIMIT = 6

data class HomeContent(
    val featured: List<Comic>,
    val newest: List<Comic>,
    val popular: List<Comic>,
    val continueReading: List<ContinueItem> = emptyList()
) {
    /** فقط فهرست آثار را می‌سنجد؛ «ادامه‌ی مطالعه» جدا حساب می‌شود. */
    val isEmpty: Boolean
        get() = featured.isEmpty() && newest.isEmpty() && popular.isEmpty()
}

fun buildHomeContent(comics: List<Comic>): HomeContent = HomeContent(
    featured = comics.filter { it.isFeatured }.sortedByDescending { it.popularity },
    newest = comics.sortedByDescending { it.addedOrder }.take(HOME_SECTION_LIMIT),
    popular = comics.sortedByDescending { it.popularity }.take(HOME_SECTION_LIMIT)
)

private fun toHomeState(
    loaded: UiState<List<Comic>>,
    progress: Map<String, ReadingProgress>
): UiState<HomeContent> = when (loaded) {
    is UiState.Loading -> UiState.Loading
    is UiState.Error -> loaded
    is UiState.Success -> UiState.Success(
        buildHomeContent(loaded.data).copy(continueReading = buildContinueList(loaded.data, progress))
    )
}

class HomeViewModel(
    private val repository: ComicRepository,
    library: UserLibraryRepository
) : ViewModel() {

    private val comics = MutableStateFlow<UiState<List<Comic>>>(UiState.Loading)

    val state: StateFlow<UiState<HomeContent>> =
        combine(comics, library.progress) { loaded, progress -> toHomeState(loaded, progress) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    init {
        load()
    }

    fun load() {
        comics.value = UiState.Loading
        viewModelScope.launch {
            repository.getComics()
                .onSuccess { list -> comics.value = UiState.Success(list) }
                .onFailure { comics.value = UiState.Error(UiMessages.LOAD_FAILED) }
        }
    }
}
