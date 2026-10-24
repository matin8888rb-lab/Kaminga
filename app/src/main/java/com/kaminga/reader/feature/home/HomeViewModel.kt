package com.kaminga.reader.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaminga.reader.core.common.UiMessages
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.repository.ComicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val HOME_SECTION_LIMIT = 6

data class HomeContent(
    val featured: List<Comic>,
    val newest: List<Comic>,
    val popular: List<Comic>
) {
    val isEmpty: Boolean
        get() = featured.isEmpty() && newest.isEmpty() && popular.isEmpty()
}

fun buildHomeContent(comics: List<Comic>): HomeContent = HomeContent(
    featured = comics.filter { it.isFeatured }.sortedByDescending { it.popularity },
    newest = comics.sortedByDescending { it.addedOrder }.take(HOME_SECTION_LIMIT),
    popular = comics.sortedByDescending { it.popularity }.take(HOME_SECTION_LIMIT)
)

class HomeViewModel(private val repository: ComicRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<HomeContent>>(UiState.Loading)
    val state: StateFlow<UiState<HomeContent>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            repository.getComics()
                .onSuccess { comics -> _state.value = UiState.Success(buildHomeContent(comics)) }
                .onFailure { _state.value = UiState.Error(UiMessages.LOAD_FAILED) }
        }
    }
}
