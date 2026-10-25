package com.kaminga.reader.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaminga.reader.core.common.UiMessages
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.domain.model.Chapter
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.ImportedChapter
import com.kaminga.reader.domain.model.ReadingProgress
import com.kaminga.reader.domain.repository.ChapterImporter
import com.kaminga.reader.domain.repository.ComicNotFoundException
import com.kaminga.reader.domain.repository.ComicRepository
import com.kaminga.reader.domain.repository.UserLibraryRepository
import com.kaminga.reader.domain.usecase.withImportedChapters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailsContent(
    val comic: Comic,
    val isFavorite: Boolean,
    val progress: ReadingProgress?
) {
    /** فصلی که «ادامه‌ی مطالعه» باید باز کند؛ اگر پیشرفت قدیمی یا فصلش حذف شده باشد null است. */
    val continueChapter: Chapter?
        get() = progress?.let { p -> comic.chapters.firstOrNull { it.id == p.chapterId && it.isReadable } }

    val firstReadableChapter: Chapter?
        get() = comic.chapters.firstOrNull { it.isReadable }
}

data class ImportUiState(
    val isImporting: Boolean = false,
    val message: String? = null
)

private fun toDetailsState(
    loaded: UiState<Comic>,
    imported: List<ImportedChapter>,
    favorites: Set<String>,
    progress: Map<String, ReadingProgress>
): UiState<DetailsContent> = when (loaded) {
    is UiState.Loading -> UiState.Loading
    is UiState.Error -> loaded
    is UiState.Success -> {
        val comic = loaded.data.withImportedChapters(imported)
        UiState.Success(DetailsContent(comic, comic.id in favorites, progress[comic.id]))
    }
}

class DetailsViewModel(
    private val repository: ComicRepository,
    private val library: UserLibraryRepository,
    private val importer: ChapterImporter,
    private val comicId: String
) : ViewModel() {

    private val base = MutableStateFlow<UiState<Comic>>(UiState.Loading)

    val state: StateFlow<UiState<DetailsContent>> =
        combine(base, library.importedChapters, library.favoriteIds, library.progress) { loaded, imported, favorites, progress ->
            toDetailsState(loaded, imported, favorites, progress)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    private val _importState = MutableStateFlow(ImportUiState())
    val importState: StateFlow<ImportUiState> = _importState.asStateFlow()

    init {
        load()
    }

    fun load() {
        base.value = UiState.Loading
        viewModelScope.launch {
            repository.getComic(comicId)
                .onSuccess { comic -> base.value = UiState.Success(comic) }
                .onFailure { error ->
                    base.value = if (error is ComicNotFoundException) {
                        UiState.Error(UiMessages.NOT_FOUND, retryable = false)
                    } else {
                        UiState.Error(UiMessages.LOAD_FAILED)
                    }
                }
        }
    }

    fun toggleFavorite() {
        val current = (state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch { library.setFavorite(comicId, !current.isFavorite) }
    }

    fun importChapter(sourceUris: List<String>) {
        if (sourceUris.isEmpty() || _importState.value.isImporting) return
        viewModelScope.launch {
            _importState.value = ImportUiState(isImporting = true)
            val existing = library.importedChapters.first().count { it.comicId == comicId }
            val title = "فصل شخصی ${existing + 1}"
            importer.importChapter(comicId, sourceUris, title)
                .onSuccess { chapter ->
                    library.addImportedChapter(chapter)
                    _importState.value = ImportUiState(message = UiMessages.IMPORT_DONE)
                }
                .onFailure {
                    _importState.value = ImportUiState(message = UiMessages.IMPORT_FAILED)
                }
        }
    }

    fun deletePersonalChapter(chapter: Chapter) {
        if (!chapter.isPersonal) return
        viewModelScope.launch {
            importer.deleteFiles(chapter.pagePaths)
            library.removeImportedChapter(chapter.id)
        }
    }

    fun dismissMessage() {
        _importState.value = ImportUiState()
    }
}
