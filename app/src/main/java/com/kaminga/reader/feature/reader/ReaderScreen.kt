package com.kaminga.reader.feature.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaminga.reader.core.common.UiState
import com.kaminga.reader.core.common.simpleFactory
import com.kaminga.reader.core.designsystem.DemoPage
import com.kaminga.reader.core.designsystem.ErrorView
import com.kaminga.reader.core.designsystem.KamingaChip
import com.kaminga.reader.core.designsystem.LoadingView
import com.kaminga.reader.di.AppContainer
import com.kaminga.reader.domain.model.Chapter
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.ReaderMode
import com.kaminga.reader.domain.model.ReadingDirection
import com.kaminga.reader.domain.model.defaultReaderMode
import com.kaminga.reader.domain.model.defaultReadingDirection
import com.kaminga.reader.ui.theme.Cyan
import com.kaminga.reader.ui.theme.Muted
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(
    comicId: String,
    chapterId: String,
    onBack: () -> Unit,
    onOpenChapter: (String) -> Unit,
    viewModel: ReaderViewModel = viewModel(
        key = "reader-$comicId-$chapterId",
        factory = simpleFactory {
            ReaderViewModel(AppContainer.comicRepository, AppContainer.userLibrary, comicId, chapterId)
        }
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        when (val current = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
                ErrorView(
                    message = current.message,
                    onRetry = if (current.retryable) viewModel::load else null
                )
            }
            is UiState.Success -> ReaderBody(
                content = current.data,
                onBack = onBack,
                onOpenChapter = onOpenChapter,
                onPageChanged = viewModel::onPageChanged
            )
        }
    }
}

@Composable
private fun ReaderBody(
    content: ReaderContent,
    onBack: () -> Unit,
    onOpenChapter: (String) -> Unit,
    onPageChanged: (Int) -> Unit
) {
    val comic = content.comic
    val chapter = content.chapter
    val pageCount = content.pageCount

    var mode by rememberSaveable(comic.id) { mutableStateOf(comic.type.defaultReaderMode()) }
    var direction by rememberSaveable(comic.id) { mutableStateOf(comic.type.defaultReadingDirection()) }
    var overlayVisible by rememberSaveable { mutableStateOf(true) }
    var currentPage by rememberSaveable(chapter.id) { mutableStateOf(content.initialPage) }
    var brightness by rememberSaveable { mutableStateOf(-1f) }

    val latestOnPageChanged by rememberUpdatedState(onPageChanged)
    val pagerState = rememberPagerState(initialPage = currentPage) { pageCount }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentPage)
    val zoom = remember { ZoomState() }
    val scope = rememberCoroutineScope()

    val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    val verticalTargetPx = screenWidthPx
    val pagedTargetPx = (screenWidthPx * 2).coerceAtMost(2400)

    ImmersiveEffect(hideBars = !overlayVisible)
    BrightnessEffect(brightness)

    // جای مطالعه را از حالت فعال می‌خوانیم و ذخیره می‌کنیم؛ با عوض شدن حالت، همان صفحه باز می‌ماند.
    LaunchedEffect(mode) {
        if (mode == ReaderMode.PAGED) {
            if (pagerState.currentPage != currentPage) pagerState.scrollToPage(currentPage)
            snapshotFlow { pagerState.currentPage }.collect { page ->
                currentPage = page
                zoom.reset()
                latestOnPageChanged(page)
            }
        } else {
            if (listState.firstVisibleItemIndex != currentPage) listState.scrollToItem(currentPage)
            snapshotFlow {
                if (listState.layoutInfo.totalItemsCount > 0 && !listState.canScrollForward) {
                    pageCount - 1
                } else {
                    listState.firstVisibleItemIndex
                }
            }.collect { page ->
                currentPage = page
                latestOnPageChanged(page)
            }
        }
    }

    val goToPage: (Int) -> Unit = { page ->
        scope.launch {
            val target = page.coerceIn(0, pageCount - 1)
            if (mode == ReaderMode.PAGED) pagerState.scrollToPage(target) else listState.scrollToItem(target)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (mode == ReaderMode.PAGED) {
            val layoutDirection =
                if (direction == ReadingDirection.RTL) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    userScrollEnabled = !zoom.isZoomed,
                    key = { index -> index }
                ) { page ->
                    ZoomablePage(
                        zoom = if (page == pagerState.currentPage) zoom else null,
                        onTap = { overlayVisible = !overlayVisible }
                    ) {
                        ReaderPage(comic, chapter, page, pageCount, pagedTargetPx, fillScreen = true)
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { overlayVisible = !overlayVisible })
                    }
            ) {
                items(pageCount, key = { index -> index }) { page ->
                    ReaderPage(comic, chapter, page, pageCount, verticalTargetPx, fillScreen = false)
                }
            }
        }

        AnimatedVisibility(
            visible = overlayVisible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ReaderTopBar(
                title = comic.title,
                subtitle = "فصل ${chapter.number} • ${chapter.title}",
                onBack = onBack
            )
        }

        AnimatedVisibility(
            visible = overlayVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ReaderBottomPanel(
                currentPage = currentPage,
                pageCount = pageCount,
                mode = mode,
                direction = direction,
                brightness = brightness,
                hasPrevious = content.previousChapterId != null,
                hasNext = content.nextChapterId != null,
                onPageSelected = goToPage,
                onModeChange = { newMode -> mode = newMode },
                onDirectionChange = { newDirection -> direction = newDirection },
                onBrightnessChange = { value -> brightness = value },
                onPrevious = { content.previousChapterId?.let(onOpenChapter) },
                onNext = { content.nextChapterId?.let(onOpenChapter) }
            )
        }
    }
}

@Composable
private fun ZoomablePage(
    zoom: ZoomState?,
    onTap: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(zoom) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { zoom?.toggle() }
                )
            }
            .zoomGestures(zoom)
            .graphicsLayer {
                val scale = zoom?.scale ?: 1f
                scaleX = scale
                scaleY = scale
                translationX = zoom?.offsetX ?: 0f
                translationY = zoom?.offsetY ?: 0f
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ReaderPage(
    comic: Comic,
    chapter: Chapter,
    page: Int,
    pageCount: Int,
    targetWidthPx: Int,
    fillScreen: Boolean
) {
    val path = chapter.pagePaths.getOrNull(page)
    val description = "صفحه ${page + 1} از $pageCount"
    if (path != null) {
        if (fillScreen) {
            FileImagePage(path, targetWidthPx, ContentScale.Fit, description, Modifier.fillMaxSize())
        } else {
            FileImagePage(path, targetWidthPx, ContentScale.FillWidth, description, Modifier.fillMaxWidth())
        }
    } else {
        if (fillScreen) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                DemoPage(comic, chapter.number, page, pageCount, Modifier.aspectRatio(2f / 3f))
            }
        } else {
            DemoPage(comic, chapter.number, page, pageCount, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
        }
    }
}

@Composable
private fun ReaderTopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.75f))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ReaderBottomPanel(
    currentPage: Int,
    pageCount: Int,
    mode: ReaderMode,
    direction: ReadingDirection,
    brightness: Float,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPageSelected: (Int) -> Unit,
    onModeChange: (ReaderMode) -> Unit,
    onDirectionChange: (ReadingDirection) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    var sliderValue by remember(currentPage) { mutableStateOf(currentPage.toFloat()) }
    val sliderColors = SliderDefaults.colors(thumbColor = Cyan, activeTrackColor = Cyan)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("صفحه ${currentPage + 1} از $pageCount", fontSize = 13.sp, color = Muted)
        if (pageCount > 1) {
            Slider(
                value = sliderValue,
                onValueChange = { value -> sliderValue = value },
                onValueChangeFinished = { onPageSelected(sliderValue.roundToInt()) },
                valueRange = 0f..(pageCount - 1).toFloat(),
                colors = sliderColors
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReaderMode.entries.forEach { option ->
                KamingaChip(option.label, mode == option) { onModeChange(option) }
            }
        }
        if (mode == ReaderMode.PAGED) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadingDirection.entries.forEach { option ->
                    KamingaChip(option.label, direction == option) { onDirectionChange(option) }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("روشنایی", fontSize = 12.sp, color = Muted)
            Slider(
                value = if (brightness < 0f) 0.5f else brightness,
                onValueChange = onBrightnessChange,
                valueRange = 0.05f..1f,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                colors = sliderColors
            )
            TextButton(onClick = { onBrightnessChange(-1f) }) { Text("پیش‌فرض") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onPrevious, enabled = hasPrevious) { Text("فصل قبلی") }
            TextButton(onClick = onNext, enabled = hasNext) { Text("فصل بعدی") }
        }
    }
}
