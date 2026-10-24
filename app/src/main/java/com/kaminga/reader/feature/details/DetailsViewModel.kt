package com.kaminga.reader.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaminga.reader.core.common.UiMessages
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.repository.ComicNotFoundException
import com.kaminga.reader.domain.repository.ComicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repository: ComicRepository,
    private val comicId: String
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Comic>>(UiState.Loading)
    val state: StateFlow<UiState<Comic>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            repository.getComic(comicId)
                .onSuccess { comic -> _state.value = UiState.Success(comic) }
                .onFailure { error ->
                    _state.value = if (error is ComicNotFoundException) {
                        UiState.Error(UiMessages.NOT_FOUND, retryable = false)
                    } else {
                        UiState.Error(UiMessages.LOAD_FAILED)
                    }
                }
        }
    }
}
