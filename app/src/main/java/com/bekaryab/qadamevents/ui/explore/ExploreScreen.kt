package com.bekaryab.qadamevents.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.data.local.SampleExploreEvents
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.ui.auth.Green
import com.bekaryab.qadamevents.ui.auth.Purple
import com.bekaryab.qadamevents.ui.events.create.EventCategories
import com.bekaryab.qadamevents.ui.events.components.rememberContentImageBitmap
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc
import kotlinx.coroutines.launch

private val ExploreNavy = Color(0xFF2E3768)
private val ExploreMuted = Color(0xFF7B8AA0)
private val FieldGray = Color(0xFF9AA3B2)

/** Compact event discovery screen. Filter/search/bookmark state lives in [ExploreViewModel]. */
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val feed by viewModel.feed.collectAsState()
    val upcomingOnly by viewModel.upcomingOnly.collectAsState()
    val selectedCategory by viewModel.category.collectAsState()
    val bookmarks by viewModel.bookmarkedIds.collectAsState()
    val popular by viewModel.popular.collectAsState()

    var filterOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun soon(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val interests = remember {
        listOf(
            ExploreInterest("Technology", R.drawable.ic_code, Color(0xFFE3E7FA), "Tech"),
            ExploreInterest("Design", R.drawable.ic_pencil, Color(0xFFDDF0E8), "Design"),
            ExploreInterest("Business", R.drawable.ic_work, Color(0xFFF7E9D8), "Startup"),
            ExploreInterest("Art", R.drawable.ic_palette, Color(0xFFEBE1F6), "Art")
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                ExploreHeader(onLocationClick = {
                    soon("Location selection is coming soon")
                })
            }

            item(key = "search") {
                SearchField(
                    query = query,
                    onQueryChange = viewModel::setQuery,
                    onFilterClick = { filterOpen = true }
                )
            }

            item(key = "feeds") {
                FeedPills(
                    selected = feed,
                    onSelect = viewModel::selectFeed
                )
            }

            item(key = "featured") {
                // +2dp so pills→featured lands in the 12–14dp band.
                Spacer(modifier = Modifier.height(2.dp))
                FeaturedCard(
                    event = SampleExploreEvents.featured,
                    onOpen = { soon("Event details are coming soon") }
                )
            }

            item(key = "interests-head") {
                SectionHead(
                    title = "Explore by interest",
                    onSeeAll = { soon("All interests are coming soon") }
                )
            }

            item(key = "interests") {
                InterestsRow(
                    interests = interests,
                    onPick = { viewModel.setQuery(it.query) }
                )
            }

            item(key = "popular-head") {
                SectionHead(
                    title = "Popular near you",
                    onSeeAll = { soon("All events are coming soon") }
                )
            }

            if (popular.isEmpty()) {
                item(key = "empty") {
                    EmptyFeed(feed = feed, query = query)
                }
            } else {
                items(popular, key = { it.id }) { event ->
                    PopularRow(
                        event = event,
                        saved = event.id in bookmarks,
                        onBookmark = { viewModel.toggleBookmark(event.id) },
                        onOpen = { soon("Event details are coming soon") }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        )
    }

    if (filterOpen) {
        AlertDialog(
            onDismissRequest = { filterOpen = false },
            title = {
                Text(
                    text = "Filters",
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ExploreNavy
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Category",
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = ExploreMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Date-range and location filters omitted: no backing data.
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setCategory(null) },
                            label = {
                                Text(
                                    text = "All",
                                    fontFamily = AlumniSansSc,
                                    fontSize = 12.sp
                                )
                            }
                        )
                        EventCategories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { viewModel.setCategory(category) },
                                label = {
                                    Text(
                                        text = category,
                                        fontFamily = AlumniSansSc,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setUpcomingOnly(!upcomingOnly) }
                    ) {
                        Checkbox(
                            checked = upcomingOnly,
                            onCheckedChange = viewModel::setUpcomingOnly
                        )
                        Text(
                            text = "Upcoming events only",
                            fontFamily = AlumniSansSc,
                            fontSize = 14.sp,
                            color = ExploreNavy
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.clearFilters()
                        filterOpen = false
                    }
                ) {
                    Text(
                        text = "Clear all",
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { filterOpen = false }) {
                    Text(
                        text = "Apply",
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
}

@Composable
private fun ExploreHeader(onLocationClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Explore",
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            lineHeight = 32.sp,
            color = ExploreNavy,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onLocationClick)
                .padding(vertical = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_place),
                contentDescription = null,
                tint = ExploreNavy,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Almaty, Kazakhstan",
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = ExploreNavy,
                maxLines = 1
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_expand_more),
                contentDescription = "Change location",
                tint = ExploreMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    // Transparent row directly on the page background: no pill, no border,
    // no shadow. The 40dp filter target sets the row height.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.tab_explore_search),
            contentDescription = null,
            tint = ExploreNavy,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = ExploreNavy
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Find events, people, interests",
                            fontFamily = AlumniSansSc,
                            fontSize = 13.sp,
                            color = FieldGray,
                            maxLines = 1
                        )
                    }
                    inner()
                }
            )
        }
        // Plain 40dp touch target: IconButton would enforce a 48dp minimum
        // and stretch the row back to pill height.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onFilterClick)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_tune),
                contentDescription = "Filters",
                tint = ExploreNavy,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FeedPills(
    selected: ExploreFeed,
    onSelect: (ExploreFeed) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExploreFeed.values().forEach { option ->
            val isSelected = option == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Purple else Color.White)
                    .clickable(onClick = { onSelect(option) })
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = option.label,
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    // Tight line box: the inherited 24sp bodyLarge line would
                    // add ~9dp of dead space inside every pill.
                    lineHeight = 15.sp,
                    color = if (isSelected) Color.White else ExploreNavy,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun SectionHead(
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = ExploreNavy,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "See all",
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Purple,
            maxLines = 1,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onSeeAll)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FeaturedCard(
    event: Event,
    onOpen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFC9D4F2), Color(0xFF9FB4DD))
                )
            )
            .clickable(onClick = onOpen)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.55f)
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FEATURED EVENT",
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                    color = Green,
                    maxLines = 1,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = event.title,
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.time,
                    fontFamily = AlumniSansSc,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.location,
                    fontFamily = AlumniSansSc,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ExploreNavy)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = "Open event",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun InterestsRow(
    interests: List<ExploreInterest>,
    onPick: (ExploreInterest) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        interests.forEach { interest ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(interest.background)
                    .clickable(onClick = { onPick(interest) })
                    .padding(horizontal = 6.dp, vertical = 12.dp)
            ) {
                Icon(
                    painter = painterResource(id = interest.iconRes),
                    contentDescription = null,
                    tint = ExploreNavy,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = interest.label,
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = ExploreNavy,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PopularRow(
    event: Event,
    saved: Boolean,
    onBookmark: () -> Unit,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PopularThumb(coverUri = event.coverImageUri, seed = event.id)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 15.sp,
                color = ExploreNavy,
                maxLines = 2,
                minLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = event.time,
                fontFamily = AlumniSansSc,
                fontSize = 11.sp,
                color = ExploreMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_place),
                    contentDescription = null,
                    tint = ExploreMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.location,
                    fontFamily = AlumniSansSc,
                    fontSize = 11.sp,
                    color = ExploreMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(
            onClick = onBookmark,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (saved) R.drawable.ic_bookmark
                    else R.drawable.ic_bookmark_outline
                ),
                contentDescription = if (saved) "Remove bookmark" else "Bookmark event",
                tint = if (saved) Purple else ExploreMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun PopularThumb(
    coverUri: String?,
    seed: String
) {
    val tones = remember {
        listOf(Color(0xFFDDE3F2), Color(0xFFD8E8E4), Color(0xFFE9E2F2))
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(96.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tones[seed.hashCode().mod(tones.size)])
    ) {
        val cover = rememberContentImageBitmap(coverUri)
        if (cover != null) {
            Image(
                bitmap = cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_schedule),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun EmptyFeed(
    feed: ExploreFeed,
    query: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                query.isNotBlank() -> "No events match “$query”"
                feed == ExploreFeed.PEOPLE -> "No people to show yet"
                feed == ExploreFeed.PLACES -> "No places to show yet"
                else -> "No events yet"
            },
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = ExploreMuted,
            textAlign = TextAlign.Center
        )
    }
}
