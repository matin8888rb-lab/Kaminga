package com.kaminga.reader.feature.library

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

data class LibraryContent(
    val favorites: List<Comic>,
    val history: List<ContinueItem>
)

private fun toLibraryState(
    loaded: UiState<List<Comic>>,
    favoriteIds: Set<String>,
    progress: Map<String, ReadingProgress>
): UiState<LibraryContent> = when (loaded) {
    is UiState.Loading -> UiState.Loading
    is UiState.Error -> loaded
    is UiState.Success -> UiState.Success(
        LibraryContent(
            favorites = loaded.data.filter { it.id in favoriteIds },
            history = buildContinueList(loaded.data, progress)
        )
    )
}

class LibraryViewModel(
    private val repository: ComicRepository,
    library: UserLibraryRepository
) : ViewModel() {

    private val comics = MutableStateFlow<UiState<List<Comic>>>(UiState.Loading)

    val state: StateFlow<UiState<LibraryContent>> =
        combine(comics, library.favoriteIds, library.progress) { loaded, favorites, progress ->
            toLibraryState(loaded, favorites, progress)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

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
