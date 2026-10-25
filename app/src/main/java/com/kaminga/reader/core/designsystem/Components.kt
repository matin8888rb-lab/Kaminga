package com.kaminga.reader.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.ui.theme.Cyan
import com.kaminga.reader.ui.theme.Muted
import com.kaminga.reader.ui.theme.Night
import com.kaminga.reader.ui.theme.Panel
import com.kaminga.reader.ui.theme.PanelHigh
import com.kaminga.reader.ui.theme.Purple

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    horizontalPadding: Dp = 18.dp
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding, end = horizontalPadding, top = 24.dp, bottom = 12.dp)
    ) {
        Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Text(subtitle, fontSize = 12.sp, color = Muted)
        }
    }
}

@Composable
fun TagChip(text: String, modifier: Modifier = Modifier) {
    Surface(color = PanelHigh, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text,
            color = Cyan,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun DemoBadge(modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = 0.45f),
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            "DEMO",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/** جلد اثر. در مرحله‌ی اول تصویر واقعی نداریم؛ گرادیان رنگی اثر + عنوان اصلی نمایش داده می‌شود. */
@Composable
fun ComicCover(
    comic: Comic,
    modifier: Modifier = Modifier,
    titleSize: TextUnit = 20.sp,
    shape: Shape = RoundedCornerShape(20.dp),
    compact: Boolean = false
) {
    Box(
        modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(comic.accentColor), Night)))
            .padding(if (compact) 6.dp else 12.dp)
    ) {
        if (!compact) {
            Text(
                comic.type.label,
                color = Cyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
        Text(
            comic.originalTitle,
            fontSize = titleSize,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center)
        )
        if (comic.isDemo && !compact) {
            DemoBadge(Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
fun ComicCard(comic: Comic, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .width(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ComicCover(comic, Modifier.fillMaxWidth().height(210.dp))
        Text(
            comic.title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${comic.type.label} • ${comic.status.label}",
            color = Muted,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ComicRow(
    comic: Comic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Panel)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ComicCover(
            comic,
            Modifier.size(width = 74.dp, height = 96.dp),
            titleSize = 11.sp,
            shape = RoundedCornerShape(12.dp),
            compact = true
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                comic.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val demoSuffix = if (comic.isDemo) " • نمایشی" else ""
            Text(
                subtitle ?: "${comic.type.label} • ${comic.status.label}$demoSuffix",
                color = Cyan,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                comic.summary,
                color = Muted,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LoadingView(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Cyan)
    }
}

@Composable
fun MessageView(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = Muted, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(14.dp))
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(body, color = Muted, fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    MessageView(
        icon = Icons.Default.Warning,
        title = "مشکلی پیش آمد",
        body = message,
        modifier = modifier,
        actionLabel = if (onRetry != null) "تلاش دوباره" else null,
        onAction = onRetry
    )
}

@Composable
fun EmptyView(
    title: String,
    body: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    icon: ImageVector = Icons.Default.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    MessageView(
        icon = icon,
        title = title,
        body = body,
        modifier = modifier,
        actionLabel = actionLabel,
        onAction = onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KamingaChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Panel,
            labelColor = Muted,
            selectedContainerColor = Purple.copy(alpha = 0.35f),
            selectedLabelColor = Color.White
        )
    )
}
