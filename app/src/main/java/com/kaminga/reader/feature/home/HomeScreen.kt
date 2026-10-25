package com.kaminga.reader.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.core.common.simpleFactory
import com.kaminga.reader.core.designsystem.ComicCard
import com.kaminga.reader.core.designsystem.ComicRow
import com.kaminga.reader.core.designsystem.EmptyView
import com.kaminga.reader.core.designsystem.ErrorView
import com.kaminga.reader.core.designsystem.LoadingView
import com.kaminga.reader.core.designsystem.SectionTitle
import com.kaminga.reader.di.AppContainer
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.usecase.describe
import com.kaminga.reader.ui.theme.Cyan
import com.kaminga.reader.ui.theme.Muted
import com.kaminga.reader.ui.theme.Night
import com.kaminga.reader.ui.theme.Panel
import com.kaminga.reader.ui.theme.Purple

@Composable
fun HomeScreen(
    onComicClick: (String) -> Unit,
    onContinueClick: (String, String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = simpleFactory {
            HomeViewModel(AppContainer.comicRepository, AppContainer.userLibrary)
        }
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        BrandHeader()
        when (val current = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(
                message = current.message,
                onRetry = if (current.retryable) viewModel::load else null
            )
            is UiState.Success -> HomeSections(current.data, onComicClick, onContinueClick, onSearchClick)
        }
    }
}

@Composable
private fun HomeSections(
    content: HomeContent,
    onComicClick: (String) -> Unit,
    onContinueClick: (String, String) -> Unit,
    onSearchClick: () -> Unit
) {
    if (content.isEmpty) {
        EmptyView(
            title = "هنوز اثری اضافه نشده",
            body = "وقتی آثار منتشر شوند، این‌جا نمایش داده می‌شوند."
        )
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item { SearchEntry(onSearchClick) }
            item {
                Column {
                    Spacer(Modifier.height(14.dp))
                    HeroBanner(onSearchClick)
                }
            }
            if (content.continueReading.isNotEmpty()) {
                item { SectionTitle("ادامه‌ی مطالعه") }
                items(content.continueReading.take(3), key = { entry -> "continue-${entry.comic.id}" }) { entry ->
                    ComicRow(
                        comic = entry.comic,
                        onClick = { onContinueClick(entry.comic.id, entry.progress.chapterId) },
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                        subtitle = entry.progress.describe()
                    )
                }
            }
            if (content.featured.isNotEmpty()) {
                item { SectionTitle("آثار منتخب", subtitle = "نمونه‌های آزمایشی رابط کاربری") }
                item { ComicCarousel(content.featured, onComicClick) }
            }
            item { SectionTitle("تازه‌ترین آثار") }
            items(content.newest, key = { comic -> "new-${comic.id}" }) { comic ->
                ComicRow(
                    comic = comic,
                    onClick = { onComicClick(comic.id) },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }
            item { SectionTitle("محبوب‌ترین‌ها") }
            item { ComicCarousel(content.popular, onComicClick) }
            item {
                Text(
                    "همه‌ی آثار این نسخه نمایشی و ساختگی هستند.",
                    color = Muted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun ComicCarousel(comics: List<Comic>, onComicClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(comics, key = { comic -> comic.id }) { comic ->
            ComicCard(comic = comic, onClick = { onComicClick(comic.id) })
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Purple, Cyan))),
            contentAlignment = Alignment.Center
        ) {
            Text("K", color = Night, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("KAMINGA", fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 17.sp)
            Text("دنیای کمیک، مانگا و مانهوا", color = Muted, fontSize = 12.sp)
        }
        Surface(color = Panel, shape = RoundedCornerShape(50)) {
            Text(
                "DEMO",
                color = Cyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
            )
        }
    }
}

/** میان‌بر به تب کشف؛ خودش جست‌وجو نمی‌کند. */
@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Muted)
        Spacer(Modifier.width(10.dp))
        Text("جست‌وجوی عنوان، نویسنده یا ژانر…", color = Muted, fontSize = 14.sp)
    }
}

@Composable
private fun HeroBanner(onExplore: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF34265F), Color(0xFF142D48), Color(0xFF111729))
                )
            )
            .padding(22.dp)
    ) {
        Text(
            "THE NEXT UNIVERSE",
            color = Cyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "داستان بعدی‌ات\nاز این‌جا شروع می‌شود",
            fontSize = 27.sp,
            lineHeight = 35.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(9.dp))
        Text(
            "کمیک، مانگا و مانهوا؛ در یک کتابخانه‌ی واحد.",
            color = Color(0xFFD3D9EA),
            fontSize = 13.sp
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onExplore,
            colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night),
            shape = RoundedCornerShape(13.dp)
        ) {
            Text("کشف آثار", fontWeight = FontWeight.Bold)
        }
    }
}
