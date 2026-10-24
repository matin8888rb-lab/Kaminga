package com.kaminga.reader.feature.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.core.common.simpleFactory
import com.kaminga.reader.core.designsystem.ComicCover
import com.kaminga.reader.core.designsystem.ErrorView
import com.kaminga.reader.core.designsystem.LoadingView
import com.kaminga.reader.core.designsystem.SectionTitle
import com.kaminga.reader.core.designsystem.TagChip
import com.kaminga.reader.di.AppContainer
import com.kaminga.reader.domain.model.Chapter
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.ui.theme.Cyan
import com.kaminga.reader.ui.theme.Muted
import com.kaminga.reader.ui.theme.Panel

@Composable
fun DetailsScreen(
    comicId: String,
    onBack: () -> Unit,
    viewModel: DetailsViewModel = viewModel(
        key = "details-$comicId",
        factory = simpleFactory { DetailsViewModel(AppContainer.comicRepository, comicId) }
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 6.dp, top = 6.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
        }
        when (val current = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(
                message = current.message,
                onRetry = if (current.retryable) viewModel::load else null
            )
            is UiState.Success -> DetailsContent(current.data)
        }
    }
}

@Composable
private fun DetailsContent(comic: Comic) {
    LazyColumn(contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp)) {
        item {
            ComicCover(
                comic,
                Modifier.fillMaxWidth().height(280.dp),
                titleSize = 32.sp,
                shape = RoundedCornerShape(28.dp)
            )
        }
        item {
            Column {
                Spacer(Modifier.height(20.dp))
                Text(comic.title, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text(comic.originalTitle, color = Cyan, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { TagChip(comic.type.label) }
                item { TagChip(comic.status.label) }
                item { TagChip(comic.language.label) }
                items(comic.genres) { genre -> TagChip(genre.label) }
            }
        }
        item {
            Column(
                Modifier
                    .padding(top = 18.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Panel)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetaLine("نویسنده", comic.author)
                MetaLine("هنرمند", comic.artist)
                MetaLine("تعداد فصل‌ها", comic.chapters.size.toString())
            }
        }
        item {
            Column {
                SectionTitle("خلاصه‌ی داستان", horizontalPadding = 0.dp)
                Text(comic.summary, color = Muted, lineHeight = 24.sp)
            }
        }
        item {
            Column(Modifier.padding(top = 20.dp)) {
                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text("شروع مطالعه")
                }
                Text(
                    "مطالعه‌گر در به‌روزرسانی بعدی اضافه می‌شود.",
                    color = Muted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        item {
            SectionTitle("فصل‌ها", horizontalPadding = 0.dp)
        }
        if (comic.chapters.isEmpty()) {
            item {
                Text(
                    "هنوز فصلی منتشر نشده است.",
                    color = Muted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            item {
                Text(
                    "صفحه‌های این فصل‌ها هنوز بارگذاری نشده‌اند.",
                    color = Muted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            items(comic.chapters, key = { chapter -> chapter.id }) { chapter ->
                ChapterRow(chapter)
            }
        }
    }
}

@Composable
private fun MetaLine(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 13.sp, modifier = Modifier.width(100.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ChapterRow(chapter: Chapter) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Panel)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("فصل ${chapter.number}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(chapter.title, color = Muted, fontSize = 12.sp)
        }
    }
}
