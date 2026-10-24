package com.kaminga.reader.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaminga.reader.core.common.UiMessages
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.SearchFilters
import com.kaminga.reader.domain.repository.ComicRepository
import com.kaminga.reader.domain.usecase.filterAndSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: ComicRepository) : ViewModel() {

    private var allComics: List<Comic> = emptyList()

    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters.asStateFlow()

    private val _state = MutableStateFlow<UiState<List<Comic>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Comic>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            repository.getComics()
                .onSuccess { comics ->
                    allComics = comics
                    publish()
                }
                .onFailure { _state.value = UiState.Error(UiMessages.LOAD_FAILED) }
        }
    }

    fun updateFilters(transform: (SearchFilters) -> SearchFilters) {
        _filters.update(transform)
        if (_state.value is UiState.Success) publish()
    }

    /** متن جست‌وجو و همه‌ی فیلترها را پاک می‌کند؛ فقط مرتب‌سازی می‌ماند. */
    fun clearFilters() {
        updateFilters { current -> SearchFilters(sort = current.sort) }
    }

    private fun publish() {
        _state.value = UiState.Success(filterAndSort(allComics, _filters.value))
    }
}
