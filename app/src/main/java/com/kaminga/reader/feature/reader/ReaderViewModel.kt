package com.kaminga.reader.feature.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaminga.reader.core.common.UiMessages
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.domain.model.Chapter
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.ReadingProgress
import com.kaminga.reader.domain.repository.ComicNotFoundException
import com.kaminga.reader.domain.repository.ComicRepository
import com.kaminga.reader.domain.repository.UserLibraryRepository
import com.kaminga.reader.domain.usecase.withImportedChapters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ReaderContent(
    val comic: Comic,
    val chapter: Chapter,
    val previousChapterId: String?,
    val nextChapterId: String?,
    val initialPage: Int
) {
    val pageCount: Int
        get() = chapter.pageCount
}

class ReaderViewModel(
    private val repository: ComicRepository,
    private val library: UserLibraryRepository,
    private val comicId: String,
    private val chapterId: String,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ReaderContent>>(UiState.Loading)
    val state: StateFlow<UiState<ReaderContent>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.getComic(comicId)
            val base = result.getOrNull()
            if (base == null) {
                _state.value = if (result.exceptionOrNull() is ComicNotFoundException) {
                    UiState.Error(UiMessages.NOT_FOUND, retryable = false)
                } else {
                    UiState.Error(UiMessages.LOAD_FAILED)
                }
                return@launch
            }

            val comic = base.withImportedChapters(library.importedChapters.first())
            val index = comic.chapters.indexOfFirst { it.id == chapterId }
            val chapter = comic.chapters.getOrNull(index)
            if (chapter == null || !chapter.isReadable) {
                _state.value = UiState.Error(UiMessages.CHAPTER_NOT_FOUND, retryable = false)
                return@launch
            }

            val saved = library.progress.first()[comicId]
            val startPage = if (saved != null && saved.chapterId == chapterId) {
                saved.pageIndex.coerceIn(0, chapter.pageCount - 1)
            } else {
                0
            }
            _state.value = UiState.Success(
                ReaderContent(
                    comic = comic,
                    chapter = chapter,
                    previousChapterId = comic.chapters.take(index).lastOrNull { it.isReadable }?.id,
                    nextChapterId = comic.chapters.drop(index + 1).firstOrNull { it.isReadable }?.id,
                    initialPage = startPage
                )
            )
        }
    }

    /** هر بار صفحه عوض شد صدا زده می‌شود و جای مطالعه ذخیره می‌شود. */
    fun onPageChanged(page: Int) {
        val content = (state.value as? UiState.Success)?.data ?: return
        val safePage = page.coerceIn(0, content.pageCount - 1)
        viewModelScope.launch {
            library.saveProgress(
                ReadingProgress(
                    comicId = comicId,
                    chapterId = chapterId,
                    chapterNumber = content.chapter.number,
                    pageIndex = safePage,
                    pageCount = content.pageCount,
                    updatedAt = clock()
                )
            )
        }
    }
}
