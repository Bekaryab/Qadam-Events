package com.bekaryab.qadamevents.ui.events.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.ui.auth.Green
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

private val CardNavy = Color(0xFF2E3768)
private val CardMuted = Color(0xFF7B8AA0)
private val InfoBackground = Color(0xFFF7F4EE)
private val IconGray = Color(0xFF9AA3B2)

// Deterministic pastel image tones; no event artwork exists yet.
private val ImageTones = listOf(
    Color(0xFFDDE3F2),
    Color(0xFFD8E8E4),
    Color(0xFFE9E2F2)
)

/**
 * Shared event card used by every Profile tab (no per-tab duplicates).
 *
 * The card shows no save/like toggle UI, but [isSaved], [isLiked],
 * [onSaveClick], and [onLikeClick] are intentionally retained: the SAVES and
 * LIKES tabs still filter on them, so tab functionality is preserved and the
 * toggles can be re-attached to future UI without signature changes.
 */
@Composable
fun ProfileEventCard(
    event: Event,
    isSaved: Boolean,
    isLiked: Boolean,
    onSaveClick: () -> Unit,
    onLikeClick: () -> Unit,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                clip = false,
                spotColor = Color.Black.copy(alpha = 0.10f),
                ambientColor = Color.Black.copy(alpha = 0.10f)
            ),
        shape = RoundedCornerShape(18.dp),
        color = InfoBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(ImageTones[event.id.hashCode().mod(ImageTones.size)])
            ) {
                // Actual cover photo when the event has one; the pastel tone
                // stays behind as the fallback while it loads or if missing.
                val cover = rememberContentImageBitmap(event.coverImageUri)
                if (cover != null) {
                    Image(
                        bitmap = cover,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (event.isUpcoming) {
                    Text(
                        text = "UPCOMING",
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp,
                        color = Green,
                        maxLines = 1,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)

                    )
                }

                Text(
                    text = event.dateBadge,
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = CardNavy,
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)

                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = event.title,
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    color = CardNavy,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(-4.dp))

                InfoLine(
                    iconRes = R.drawable.ic_place,
                    contentDescription = "Location",
                    text = event.location
                )

                Spacer(modifier = Modifier.height(1.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_schedule),
                            contentDescription = null,
                            tint = IconGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.time,
                            fontFamily = AlumniSansSc,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = CardMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardNavy)
                            // No details destination exists yet; kept ready.
                            .clickable(onClick = onOpenClick)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_forward),
                            contentDescription = "Open event details",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(
    iconRes: Int,
    contentDescription: String,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = IconGray,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = CardMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Two-column event grid shared by the MY EVENTS, SAVES, and LIKES tabs. */
@Composable
fun EventsGrid(
    events: List<Event>,
    savedIds: Set<String>,
    likedIds: Set<String>,
    onSaveToggle: (String) -> Unit,
    onLikeToggle: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(events, key = { it.id }) { event ->
            ProfileEventCard(
                event = event,
                isSaved = event.id in savedIds,
                isLiked = event.id in likedIds,
                onSaveClick = { onSaveToggle(event.id) },
                onLikeClick = { onLikeToggle(event.id) },
                onOpenClick = { onOpenDetails(event.id) }
            )
        }
    }
}

/** Centered empty state for tabs with no items yet. */
@Composable
fun EventsEmptyState(
    message: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = IconGray,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = CardMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
