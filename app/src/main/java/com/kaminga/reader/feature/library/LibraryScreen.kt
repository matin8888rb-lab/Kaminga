package com.kaminga.reader.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.core.common.simpleFactory
import com.kaminga.reader.core.designsystem.ComicRow
import com.kaminga.reader.core.designsystem.EmptyView
import com.kaminga.reader.core.designsystem.ErrorView
import com.kaminga.reader.core.designsystem.KamingaChip
import com.kaminga.reader.core.designsystem.LoadingView
import com.kaminga.reader.di.AppContainer
import com.kaminga.reader.domain.usecase.describe

private enum class LibrarySection(val label: String) {
    FAVORITES("علاقه‌مندی‌ها"),
    HISTORY("ادامه‌ی مطالعه")
}

@Composable
fun LibraryScreen(
    onComicClick: (String) -> Unit,
    onContinueClick: (String, String) -> Unit,
    viewModel: LibraryViewModel = viewModel(
        factory = simpleFactory { LibraryViewModel(AppContainer.comicRepository, AppContainer.userLibrary) }
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableStateOf(LibrarySection.FAVORITES) }

    Column(Modifier.fillMaxSize()) {
        Text(
            "کتابخانه‌ی من",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 22.dp)
        )
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LibrarySection.entries.forEach { option ->
                KamingaChip(option.label, section == option) { section = option }
            }
        }
        when (val current = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(
                message = current.message,
                onRetry = if (current.retryable) viewModel::load else null
            )
            is UiState.Success -> LibraryList(current.data, section, onComicClick, onContinueClick)
        }
    }
}

@Composable
private fun LibraryList(
    content: LibraryContent,
    section: LibrarySection,
    onComicClick: (String) -> Unit,
    onContinueClick: (String, String) -> Unit
) {
    if (section == LibrarySection.FAVORITES) {
        if (content.favorites.isEmpty()) {
            EmptyView(
                icon = Icons.Default.Favorite,
                title = "هنوز اثری در علاقه‌مندی‌ها نیست",
                body = "در صفحه‌ی هر اثر روی قلب بزن تا این‌جا بیاید."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(content.favorites, key = { comic -> "fav-${comic.id}" }) { comic ->
                    ComicRow(comic = comic, onClick = { onComicClick(comic.id) })
                }
            }
        }
    } else {
        if (content.history.isEmpty()) {
            EmptyView(
                icon = Icons.Default.Info,
                title = "هنوز مطالعه‌ای شروع نکرده‌ای",
                body = "بعد از باز کردن یک فصل، جای مطالعه‌ات این‌جا ذخیره می‌شود."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(content.history, key = { entry -> "history-${entry.comic.id}" }) { entry ->
                    ComicRow(
                        comic = entry.comic,
                        onClick = { onContinueClick(entry.comic.id, entry.progress.chapterId) },
                        subtitle = entry.progress.describe()
                    )
                }
            }
        }
    }
}
