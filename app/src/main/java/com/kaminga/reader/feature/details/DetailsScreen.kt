package com.kaminga.reader.feature.details

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.kaminga.reader.domain.usecase.describe
import com.kaminga.reader.ui.theme.Cyan
import com.kaminga.reader.ui.theme.Muted
import com.kaminga.reader.ui.theme.Night
import com.kaminga.reader.ui.theme.Panel

@Composable
fun DetailsScreen(
    comicId: String,
    onBack: () -> Unit,
    onOpenReader: (String, String) -> Unit,
    viewModel: DetailsViewModel = viewModel(
        key = "details-$comicId",
        factory = simpleFactory {
            DetailsViewModel(
                AppContainer.comicRepository,
                AppContainer.userLibrary,
                AppContainer.chapterImporter,
                comicId
            )
        }
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        viewModel.importChapter(uris.map { uri -> uri.toString() })
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
            }
            Spacer(Modifier.weight(1f))
            val loaded = (state as? UiState.Success)?.data
            if (loaded != null) {
                IconButton(onClick = viewModel::toggleFavorite) {
                    Icon(
                        imageVector = if (loaded.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (loaded.isFavorite) "حذف از علاقه‌مندی‌ها" else "افزودن به علاقه‌مندی‌ها",
                        tint = if (loaded.isFavorite) Cyan else Muted
                    )
                }
            }
        }
        when (val current = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(
                message = current.message,
                onRetry = if (current.retryable) viewModel::load else null
            )
            is UiState.Success -> DetailsBody(
                content = current.data,
                importState = importState,
                onOpenReader = onOpenReader,
                onImportClick = { picker.launch(arrayOf("image/*")) },
                onDeleteChapter = viewModel::deletePersonalChapter
            )
        }
    }
}

@Composable
private fun DetailsBody(
    content: DetailsContent,
    importState: ImportUiState,
    onOpenReader: (String, String) -> Unit,
    onImportClick: () -> Unit,
    onDeleteChapter: (Chapter) -> Unit
) {
    val comic = content.comic
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
            ReadActions(content, importState, onOpenReader, onImportClick)
        }
        item {
            SectionTitle("فصل‌ها", horizontalPadding = 0.dp)
        }
        if (comic.chapters.isEmpty()) {
            item {
                Text(
                    "هنوز فصلی منتشر نشده است. می‌توانی با دکمه‌ی بالا یک فصل شخصی از عکس‌های گوشی اضافه کنی.",
                    color = Muted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(comic.chapters, key = { chapter -> chapter.id }) { chapter ->
                ChapterRow(
                    chapter = chapter,
                    isCurrent = content.progress?.chapterId == chapter.id,
                    onClick = { onOpenReader(comic.id, chapter.id) },
                    onDelete = { onDeleteChapter(chapter) }
                )
            }
        }
    }
}

@Composable
private fun ReadActions(
    content: DetailsContent,
    importState: ImportUiState,
    onOpenReader: (String, String) -> Unit,
    onImportClick: () -> Unit
) {
    val comic = content.comic
    val resume = content.continueChapter
    val target = resume ?: content.firstReadableChapter
    val progress = content.progress

    Column(Modifier.padding(top = 20.dp)) {
        if (target != null) {
            Button(
                onClick = { onOpenReader(comic.id, target.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
            ) {
                val label = if (resume != null && progress != null) {
                    "ادامه‌ی مطالعه • ${progress.describe()}"
                } else {
                    "شروع مطالعه"
                }
                Text(label, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("شروع مطالعه")
            }
            Text(
                "هنوز فصل قابل‌مطالعه‌ای وجود ندارد.",
                color = Muted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        OutlinedButton(
            onClick = onImportClick,
            enabled = !importState.isImporting,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        ) {
            Text(if (importState.isImporting) "در حال افزودن…" else "افزودن فصل از عکس‌های گوشی")
        }
        val message = importState.message
        if (message != null) {
            Text(message, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
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
private fun ChapterRow(
    chapter: Chapter,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Panel)
            .clickable(enabled = chapter.isReadable, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("فصل ${chapter.number}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            val suffix = if (chapter.isPersonal) " • شخصی" else ""
            Text("${chapter.title} • ${chapter.pageCount} صفحه$suffix", color = Muted, fontSize = 12.sp)
        }
        if (isCurrent) {
            TagChip("ادامه")
        }
        if (chapter.isPersonal) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف فصل شخصی", tint = Muted)
            }
        }
    }
}
