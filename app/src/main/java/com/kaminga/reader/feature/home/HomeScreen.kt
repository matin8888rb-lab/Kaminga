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
import com.kaminga.reader.ui.theme.Cyan
import com.kaminga.reader.ui.theme.Muted
import com.kaminga.reader.ui.theme.Night
import com.kaminga.reader.ui.theme.Panel
import com.kaminga.reader.ui.theme.Purple

private val SpaceDeep = Color(0xFF050817)
private val SpaceBlue = Color(0xFF101D3B)
private val SpaceViolet = Color(0xFF38216B)
private val SpaceBorder = Color(0xFF29375C)
private val StarWhite = Color(0xFFEAF1FF)

@Composable
fun HomeScreen(
    onComicClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = simpleFactory {
            HomeViewModel(AppContainer.comicRepository)
        }
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SpaceDeep, Night, Color(0xFF0B1025))
                )
            )
    ) {
        Column(Modifier.fillMaxSize()) {
            BrandHeader()

            when (val current = state) {
                is UiState.Loading -> LoadingView()

                is UiState.Error -> ErrorView(
                    message = current.message,
                    onRetry = if (current.retryable) {
                        viewModel::load
                    } else null
                )

                is UiState.Success -> HomeSections(
                    content = current.data,
                    onComicClick = onComicClick,
                    onSearchClick = onSearchClick
                )
            }
        }
    }
}

@Composable
private fun HomeSections(
    content: HomeContent,
    onComicClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    if (content.isEmpty) {
        EmptyView(
            title = "هنوز اثری اضافه نشده",
            body = "وقتی آثار منتشر شوند، این‌جا نمایش داده می‌شوند."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                SearchEntry(onSearchClick)
            }

            item {
                Spacer(Modifier.height(14.dp))
                HeroBanner(onSearchClick)
                Spacer(Modifier.height(10.dp))
            }

            if (content.featured.isNotEmpty()) {
                item {
                    SectionTitle(
                        "آثار منتخب",
                        subtitle = "جهان‌هایی برای کشف کردن"
                    )
                }

                item {
                    ComicCarousel(content.featured, onComicClick)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle(
                    "تازه‌ترین آثار",
                    subtitle = "تازه به جهان کمینگا رسیده‌اند"
                )
            }

            items(
                content.newest,
                key = { comic -> "new-${comic.id}" }
            ) { comic ->
                ComicRow(
                    comic = comic,
                    onClick = { onComicClick(comic.id) },
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 6.dp
                    )
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle(
                    "محبوب‌ترین‌ها",
                    subtitle = "داستان‌هایی که ارزش کشف دارند"
                )
            }

            item {
                ComicCarousel(content.popular, onComicClick)
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "✦  K A M I N G A  ✦",
                        color = Cyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "این نسخه شامل آثار نمایشی و ساختگی است.",
                        color = Muted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ComicCarousel(
    comics: List<Comic>,
    onComicClick: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(
            comics,
            key = { comic -> comic.id }
        ) { comic ->
            ComicCard(
                comic = comic,
                onClick = { onComicClick(comic.id) }
            )
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF080D20),
                        Color(0xFF11152D),
                        Color(0xFF080D20)
                    )
                )
            )
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 18.dp,
                bottom = 15.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Cyan, Purple, Color(0xFF3867FF))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "K",
                color = SpaceDeep,
                fontWeight = FontWeight.Black,
                fontSize = 25.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                "KAMINGA",
                color = StarWhite,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(3.dp))

            Text(
                "EXPLORE YOUR NEXT UNIVERSE",
                color = Cyan,
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
        }

        Surface(
            color = Color(0xFF171C38),
            shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                SpaceBorder
            )
        ) {
            Text(
                "DEMO",
                color = Cyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = 11.dp,
                    vertical = 7.dp
                )
            )
        }
    }
}

/** میان‌بر به بخش جست‌وجو؛ جست‌وجو در صفحه‌ی خودش انجام می‌شود. */
@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF121A34), Color(0xFF0C142A))
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = "جست‌وجو",
            tint = Cyan,
            modifier = Modifier.size(21.dp)
        )

        Spacer(Modifier.width(11.dp))

        Text(
            "جست‌وجوی عنوان، نویسنده یا ژانر...",
            color = Muted,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            "⌕",
            color = Purple,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun HeroBanner(onExplore: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF342064),
                        Color(0xFF17264D),
                        Color(0xFF0A1830),
                        Color(0xFF101329)
                    )
                )
            )
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0x33030717)
                    )
                )
            )
            .padding(21.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Cyan)
            )

            Spacer(Modifier.width(7.dp))

            Text(
                "YOUR NEXT ADVENTURE",
                color = Cyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.7.sp
            )
        }

        Spacer(Modifier.height(17.dp))

        Text(
            "به دنیایی\nبی‌پایان خوش آمدی!",
            color = Color.White,
            fontSize = 26.sp,
            lineHeight = 37.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "از کمیک و مانگا تا مانهوا؛\nماجراجویی بعدی تو همین‌جاست.",
            color = Color(0xFFD0D9F3),
            fontSize = 13.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(19.dp))

        Button(
            onClick = onExplore,
            colors = ButtonDefaults.buttonColors(
                containerColor = Cyan,
                contentColor = SpaceDeep
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 11.dp
            )
        ) {
            Text(
                "شروع ماجراجویی  →",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(15.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "✦  ✧  ·  ✦  ·  ✧",
                color = Color(0xFFB9A5FF),
                fontSize = 13.sp,
                letterSpacing = 3.sp
            )
        }
    }
}
