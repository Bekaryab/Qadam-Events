package com.bekaryab.qadamevents.ui.events.list

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.ui.events.create.EventCategories
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

private val EventsBackground = Color(0xFFDDF3F5)
private val EventsNavy = Color(0xFF2E3768)
private val EventsMuted = Color(0xFF7B8AA0)
private val EventsBorder = Color(0xFFD4D4D4)
private val EventsPurple = Color(0xFF6F49BF)

/**
 * Stateless SIS 4 Events screen: renders [uiState] and emits user actions
 * through callbacks. Never touches a ViewModel, repository, or use case, and
 * performs no filtering itself. State priority: Loading > Error > Empty >
 * Content, so only one state is ever visible.
 */
@Composable
fun EventsScreen(
    uiState: EventsUiState,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EventsBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Events",
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            lineHeight = 28.sp,
            color = EventsNavy,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search events",
                    fontFamily = AlumniSansSc,
                    fontSize = 13.sp,
                    color = EventsMuted
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.tab_explore_search),
                    contentDescription = null,
                    tint = EventsMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = EventsPurple,
                unfocusedBorderColor = EventsBorder
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "all") {
                CategoryChip(
                    label = "All",
                    selected = uiState.selectedCategory == null,
                    onClick = { onCategorySelected(null) }
                )
            }
            items(EventCategories, key = { it }) { category ->
                CategoryChip(
                    label = category,
                    selected = uiState.selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                uiState.isLoading -> LoadingContent()

                uiState.errorMessage != null -> ErrorContent(
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )

                uiState.visibleEvents.isEmpty() -> EmptyContent()

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.visibleEvents, key = { it.id }) { event ->
                        EventRow(
                            event = event,
                            isFavorite = event.id in uiState.favoriteEventIds,
                            onFavoriteClick = { onFavoriteClick(event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) EventsPurple else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            color = if (selected) Color.White else EventsNavy,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun EventRow(
    event: Event,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = EventsNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${event.category} · ${event.location}",
                fontFamily = AlumniSansSc,
                fontSize = 12.sp,
                color = EventsMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (isFavorite) R.drawable.ic_heart
                    else R.drawable.ic_heart_outline
                ),
                contentDescription = if (isFavorite) "Remove from favorites"
                else "Add to favorites",
                tint = if (isFavorite) EventsPurple else EventsMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontFamily = AlumniSansSc,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = EventsPurple)
        ) {
            Text(
                text = "Retry",
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_schedule),
            contentDescription = null,
            tint = EventsMuted,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No events found",
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = EventsMuted,
            textAlign = TextAlign.Center
        )
    }
}

private fun previewEvents() = listOf(
    Event(
        id = "preview-1",
        title = "KBTU Hackathon 2026",
        location = "Almaty, KBTU",
        time = "25 Oct 2026 · 10:00",
        dateBadge = "25 OCT",
        description = "Code all night",
        category = "Hackathon"
    ),
    Event(
        id = "preview-2",
        title = "Design Meetup",
        location = "Almaty, Most Hub",
        time = "05 Nov 2026 · 18:00",
        dateBadge = "05 NOV",
        description = "Pixels and prototypes",
        category = "Meetup"
    )
)

@Preview(showBackground = true)
@Composable
private fun EventsLoadingPreview() {
    MaterialTheme {
        EventsScreen(
            uiState = EventsUiState(isLoading = true),
            onSearchQueryChanged = {},
            onCategorySelected = {},
            onFavoriteClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventsContentPreview() {
    MaterialTheme {
        EventsScreen(
            uiState = EventsUiState(
                allEvents = previewEvents(),
                visibleEvents = previewEvents(),
                searchQuery = "hack",
                favoriteEventIds = setOf("preview-1")
            ),
            onSearchQueryChanged = {},
            onCategorySelected = {},
            onFavoriteClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventsEmptyPreview() {
    MaterialTheme {
        EventsScreen(
            uiState = EventsUiState(),
            onSearchQueryChanged = {},
            onCategorySelected = {},
            onFavoriteClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventsErrorPreview() {
    MaterialTheme {
        EventsScreen(
            uiState = EventsUiState(errorMessage = "Couldn't load events. Try again."),
            onSearchQueryChanged = {},
            onCategorySelected = {},
            onFavoriteClick = {},
            onRetry = {}
        )
    }
}
