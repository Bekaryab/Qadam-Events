package com.bekaryab.qadamevents.ui.reels

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc
import kotlinx.coroutines.launch

private val MediaTop = Color(0xFF26235C)
private val MediaBottom = Color(0xFF0E2E33)
private val Accent = Color(0xFF7357FF)

/**
 * Immersive vertical Reels mockup. Renders inside the home content area above
 * the unchanged [com.bekaryab.qadamevents.ui.navigation.QadamBottomBar].
 * Media is a local gradient placeholder (see [ReelMedia]) until real assets land.
 *
 * @param bottomOverlay lifts the bottom content, toolbar tail, and snackbar
 * above an overlaid translucent bar. Defaults to 0 (current stacked layout).
 * Media, scrims, and top controls always stay full-bleed.
 */
@Composable
fun ReelsScreen(
    viewModel: ReelsViewModel,
    modifier: Modifier = Modifier,
    bottomOverlay: Dp = 0.dp
) {
    val state by viewModel.uiState.collectAsState()
    val demo = viewModel.demoFor(state.feed)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun placeholder(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MediaBottom)
    ) {
        ReelMedia()

        // Readability scrims: subtle top shade, stronger bottom gradient.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.35f),
                        0.25f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.65f)
                    )
                )
        )

        TopControls(
            feed = state.feed,
            onFeedSelected = viewModel::selectFeed,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        )

        ReelToolbar(
            state = state,
            demo = demo,
            onLikeToggle = viewModel::toggleLike,
            onCommentsOpen = { viewModel.setCommentsOpen(true) },
            onShare = {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${demo.title} via Qadam Events")
                }
                context.startActivity(Intent.createChooser(send, "Share reel"))
            },
            onSaveToggle = viewModel::toggleSave,
            onMoreOpen = { viewModel.setMoreOpen(true) },
            bottomOverlay = bottomOverlay,
            // Bottom-anchored (was vertically centered): the whole toolbar
            // now hugs the lower half on every screen height. BottomContent
            // keeps 76dp end clearance, so extending alongside the event
            // card/progress zone cannot collide with them.
            modifier = Modifier.align(Alignment.BottomEnd)
        )

        BottomContent(
            state = state,
            demo = demo,
            onFollowToggle = viewModel::toggleFollow,
            onEventOpen = { placeholder("Event details are coming soon") },
            bottomOverlay = bottomOverlay,
            modifier = Modifier.align(Alignment.BottomStart)
        )

        if (state.commentsOpen) {
            CommentsDialog(onDismiss = { viewModel.setCommentsOpen(false) })
        }

        if (state.moreOpen) {
            MoreMenu(
                onDismiss = { viewModel.setMoreOpen(false) },
                onPlaceholder = {
                    viewModel.setMoreOpen(false)
                    placeholder("$it is coming soon")
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp + bottomOverlay)
        )
    }
}

/**
 * Placeholder media: layered gradient + soft shapes. Replace this composable
 * with a real Image (ContentScale.Crop) or video player when assets exist.
 */
@Composable
private fun ReelMedia(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MediaTop, MediaBottom)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(
                    Color.White.copy(alpha = 0.06f),
                    CircleShape
                )
        )
        Icon(
            painter = painterResource(id = R.drawable.tab_reels_video),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.18f),
            modifier = Modifier.size(96.dp)
        )
    }
}

@Composable
private fun TopControls(
    feed: ReelFeed,
    onFeedSelected: (ReelFeed) -> Unit,
    modifier: Modifier = Modifier
) {
    // Single shared band: the old separate 32dp search row is gone, so the
    // selector sits ~32dp higher, directly below the status inset. Search
    // keeps its top-right position and remains a no-op, exactly as before.
    // No dot, underline, or marker anywhere: selection is text color only.
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReelFeed.values().forEach { option ->
                FeedLabel(
                    option = option,
                    selected = option == feed,
                    onClick = { onFeedSelected(option) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = { })
        ) {
            Icon(
                painter = painterResource(id = R.drawable.tab_explore_search),
                contentDescription = "Search reels",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RowScope.FeedLabel(
    option: ReelFeed,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = option.label,
        fontFamily = AlumniSansSc,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = if (selected) Accent else Color.White.copy(alpha = 0.75f),
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    )
}

@Composable
private fun ReelToolbar(
    state: ReelsUiState,
    demo: ReelDemo,
    onLikeToggle: () -> Unit,
    onCommentsOpen: () -> Unit,
    onShare: () -> Unit,
    onSaveToggle: () -> Unit,
    onMoreOpen: () -> Unit,
    bottomOverlay: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val likeCount = demo.baseLikes + if (state.liked) 1 else 0
    Column(
        // 16dp above the region bottom: More lands just above the progress
        // indicator on typical phones. No top gap: Like leads the column.
        modifier = modifier.padding(end = 8.dp, bottom = 16.dp + bottomOverlay),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ToolbarAction(
            iconRes = if (state.liked) R.drawable.ic_heart else R.drawable.ic_heart_outline,
            contentDescription = "Like reel",
            label = ReelsViewModel.formatCount(likeCount),
            onClick = onLikeToggle
        )

        ToolbarAction(
            iconRes = R.drawable.ic_comment,
            contentDescription = "Open comments",
            label = ReelsViewModel.formatCount(demo.comments),
            onClick = onCommentsOpen
        )

        ToolbarAction(
            iconRes = R.drawable.ic_share,
            contentDescription = "Share reel",
            label = ReelsViewModel.formatCount(demo.shares),
            onClick = onShare
        )

        ToolbarAction(
            iconRes = if (state.saved) R.drawable.ic_bookmark else R.drawable.ic_bookmark_outline,
            contentDescription = "Save reel",
            label = "Save",
            onClick = onSaveToggle
        )

        IconButton(
            onClick = onMoreOpen,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_vert),
                contentDescription = "More actions",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ToolbarAction(
    iconRes: Int,
    contentDescription: String,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun BottomContent(
    state: ReelsUiState,
    demo: ReelDemo,
    onFollowToggle: () -> Unit,
    onEventOpen: () -> Unit,
    bottomOverlay: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 76.dp, bottom = 10.dp + bottomOverlay)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Small unobtrusive placeholder glyph; no filled container.
            Icon(
                painter = painterResource(id = R.drawable.tab_profile_person),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = demo.creatorName,
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = demo.timeLocation,
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.70f),
                    maxLines = 1
                )
            }
            FollowText(
                followed = state.followed,
                onClick = onFollowToggle
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = demo.title,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            lineHeight = 20.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = demo.caption,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            color = Color.White.copy(alpha = 0.90f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinkedEventCard(
            demo = demo,
            onOpen = onEventOpen
        )

        Spacer(modifier = Modifier.height(8.dp))

        ReelProgress(progress = 0.35f)
    }
}

@Composable
private fun FollowText(
    followed: Boolean,
    onClick: () -> Unit
) {
    // Text-only action: no pill, background, border, or shadow.
    Text(
        text = if (followed) "Following" else "Follow",
        fontFamily = AlumniSansSc,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = if (followed) Color.White.copy(alpha = 0.60f) else Color.White,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    )
}

@Composable
private fun LinkedEventCard(
    demo: ReelDemo,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Flat info row directly over the media: no card fill, border, shadow,
    // thumbnail tile, or arrow background. onOpen passthrough preserved.
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_schedule),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.80f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = demo.event.title,
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = demo.event.time,
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = Color.White.copy(alpha = 0.80f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            EventMetaLine(
                iconRes = R.drawable.ic_place,
                text = demo.event.location
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onOpen)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "Open event",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun EventMetaLine(
    iconRes: Int,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.80f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.80f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReelProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
        )
    }
}

@Composable
private fun CommentsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Comments (demo)",
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Aigerim • That atmosphere was unreal!",
                    fontFamily = AlumniSansSc,
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Dias • Who is joining next year?",
                    fontFamily = AlumniSansSc,
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Commenting is not connected yet.",
                    fontFamily = AlumniSansSc,
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.50f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    )
}

@Composable
private fun MoreMenu(
    onDismiss: () -> Unit,
    onPlaceholder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Anchored near the toolbar; actions are explicit placeholders.
    Box(modifier = modifier.padding(end = 56.dp)) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = onDismiss
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Report",
                        fontFamily = AlumniSansSc,
                        fontSize = 14.sp
                    )
                },
                onClick = { onPlaceholder("Report") }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Not interested",
                        fontFamily = AlumniSansSc,
                        fontSize = 14.sp
                    )
                },
                onClick = { onPlaceholder("Not interested") }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Copy link",
                        fontFamily = AlumniSansSc,
                        fontSize = 14.sp
                    )
                },
                onClick = { onPlaceholder("Copy link") }
            )
        }
    }
}
