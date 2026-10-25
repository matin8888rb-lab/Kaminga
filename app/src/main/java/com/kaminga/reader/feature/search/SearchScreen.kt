package com.kaminga.reader.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.kaminga.reader.domain.model.ComicLanguage
import com.kaminga.reader.domain.model.ComicType
import com.kaminga.reader.domain.model.Genre
import com.kaminga.reader.domain.model.PublicationStatus
import com.kaminga.reader.domain.model.SortOrder
import com.kaminga.reader.ui.theme.Muted
import com.kaminga.reader.ui.theme.OutlineColor
import com.kaminga.reader.ui.theme.Panel
import com.kaminga.reader.ui.theme.Purple

@Composable
fun SearchScreen(
    onComicClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(
        factory = simpleFactory { SearchViewModel(AppContainer.comicRepository) }
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()

    // متن فیلد محلی نگه داشته می‌شود تا هنگام تایپ فارسی مکان‌نما نپرد.
    var queryText by rememberSaveable { mutableStateOf(filters.query) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "کشف آثار",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 22.dp)
            )
        }
        item {
            OutlinedTextField(
                value = queryText,
                onValueChange = { text ->
                    queryText = text
                    viewModel.updateFilters { current -> current.copy(query = text) }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                placeholder = { Text("نام اثر، نویسنده یا ژانر…", color = Muted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (queryText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                queryText = ""
                                viewModel.updateFilters { current -> current.copy(query = "") }
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن متن")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = OutlineColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
        item {
            ChipRow(
                options = ComicType.entries,
                selected = filters.type,
                label = { type -> type.label },
                onSelect = { type -> viewModel.updateFilters { current -> current.copy(type = type) } }
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOrder.entries.forEach { order ->
                    KamingaChip(order.label, filters.sort == order) {
                        viewModel.updateFilters { current -> current.copy(sort = order) }
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { expanded = !expanded }) {
                    val count = filters.activeFilterCount
                    val suffix = if (count > 0) " ($count)" else ""
                    Text(if (expanded) "بستن فیلترها" else "فیلترهای بیشتر$suffix")
                }
            }
        }
        if (expanded) {
            item {
                ChipRow(
                    options = Genre.entries,
                    selected = filters.genre,
                    label = { genre -> genre.label },
                    onSelect = { genre -> viewModel.updateFilters { current -> current.copy(genre = genre) } },
                    allLabel = "همه‌ی ژانرها"
                )
            }
            item {
                ChipRow(
                    options = PublicationStatus.entries,
                    selected = filters.status,
                    label = { status -> status.label },
                    onSelect = { status -> viewModel.updateFilters { current -> current.copy(status = status) } },
                    allLabel = "همه‌ی وضعیت‌ها"
                )
            }
            item {
                ChipRow(
                    options = ComicLanguage.entries,
                    selected = filters.language,
                    label = { language -> language.label },
                    onSelect = { language -> viewModel.updateFilters { current -> current.copy(language = language) } },
                    allLabel = "همه‌ی زبان‌ها"
                )
            }
        }
        if (filters.hasActiveFilters) {
            item {
                TextButton(
                    onClick = {
                        queryText = ""
                        viewModel.clearFilters()
                    },
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text("پاک کردن همه‌ی فیلترها")
                }
            }
        }

        when (val current = state) {
            is UiState.Loading -> item {
                LoadingView(Modifier.fillMaxWidth().height(240.dp))
            }
            is UiState.Error -> item {
                ErrorView(
                    message = current.message,
                    onRetry = if (current.retryable) viewModel::load else null,
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                )
            }
            is UiState.Success -> {
                if (current.data.isEmpty()) {
                    item {
                        EmptyView(
                            title = "اثری پیدا نشد",
                            body = "عبارت دیگری امتحان کن یا فیلترها را کم کن.",
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        )
                    }
                } else {
                    item {
                        Text(
                            "${current.data.size} اثر",
                            color = Muted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                    items(current.data, key = { comic -> comic.id }) { comic ->
                        ComicRow(
                            comic = comic,
                            onClick = { onComicClick(comic.id) },
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> ChipRow(
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T?) -> Unit,
    allLabel: String = "همه"
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { KamingaChip(allLabel, selected == null) { onSelect(null) } }
        items(options) { option ->
            KamingaChip(label(option), option == selected) { onSelect(option) }
        }
    }
}
