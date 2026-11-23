package com.bekaryab.qadamevents.ui.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PageSize
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import com.bekaryab.qadamevents.ui.auth.Purple
import com.bekaryab.qadamevents.ui.events.create.EventCategories
import com.bekaryab.qadamevents.ui.events.components.rememberContentImageBitmap
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc
import kotlinx.coroutines.delay
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
    val communityPosts by viewModel.communityPosts.collectAsState()
    val popular by viewModel.popular.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCommunityPosts()
    }

    var filterOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun soon(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val interests = remember {
        listOf(
            ExploreInterest("Technology", R.drawable.ic_code, Color.White, "Tech"),
            ExploreInterest("Design", R.drawable.ic_pencil, Color.White, "Design"),
            ExploreInterest("Business", R.drawable.ic_work, Color.White, "Startup"),
            ExploreInterest("Art", R.drawable.ic_palette, Color.White, "Art")
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                ExploreLocationHeader(
                    viewModel = viewModel
                )
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
                FeaturedCarousel(
                    events = SampleExploreEvents.popular,
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

            if (feed == ExploreFeed.FOR_YOU) {
                item(key = "community-head") {
                    SectionHead(
                        title = "Community Posts",
                        onSeeAll = { soon("All community posts are coming soon") }
                    )
                }

                when (val state = communityPosts) {
                    CommunityPostsUiState.Loading -> item(key = "community-loading") {
                        CommunityPostsLoading()
                    }

                    is CommunityPostsUiState.Error -> item(key = "community-error") {
                        CommunityPostsError(
                            message = state.message,
                            onRetry = viewModel::retryCommunityPosts
                        )
                    }

                    is CommunityPostsUiState.Success -> if (state.posts.isEmpty()) {
                        item(key = "community-empty") {
                            CommunityPostsEmpty(query = query)
                        }
                    } else {
                        items(state.posts, key = { "post-${it.id}" }) { post ->
                            CommunityPostCard(
                                post = post,
                                saved = post.id in bookmarks,
                                onBookmark = { viewModel.toggleBookmark(post.id) },
                                onOpen = { soon("Post details are coming soon") }
                            )
                        }
                    }
                }
            } else {
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
    // Local UI chrome state (like filterOpen below): collapsing never changes
    // the selected feed, so content is preserved across collapse/restore.
    var collapsed by remember { mutableStateOf(false) }
    val options = ExploreFeed.values()
    val selectedIndex = options.indexOf(selected)
    // One shared 400ms fraction drives every pill's slide + fade together, so
    // they stay synchronized with no per-item timing drift.
    val collapseFraction by animateFloatAsState(
        targetValue = if (collapsed) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "feed-collapse"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Exact pitch math preserves the original 8dp gaps: each pill is
        // (maxWidth - 3*gap)/4 wide. All four slots stay laid out at all
        // times, so restore lands on the EXACT original positions and no
        // duplicates or overlapping copies ever render.
        val gap = 8.dp
        val pillW = (maxWidth - gap * 3f) / 4f
        val pitch = pillW + gap
        val density = LocalDensity.current
        val pitchPx = with(density) { pitch.toPx() }
        val rowPx = with(density) { maxWidth.toPx() }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = option == selected
                // Selected pill ends at the left edge: its slot starts at
                // index*pitch, so it travels left by exactly that far. EVERY
                // other pill (including ones left of it) exits right, past
                // the row end — never leaving a gap on the left.
                val travelPx = when {
                    isSelected -> -(index * pitchPx)
                    else -> rowPx - index * pitchPx
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            translationX = travelPx * collapseFraction
                            alpha = if (isSelected) 1f else 1f - collapseFraction
                        }
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Purple else Color.White)
                        .clickable(
                            enabled = !collapsed || isSelected,
                            onClick = {
                                onSelect(option)
                                collapsed = true
                            }
                        )
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = option.label,
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
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

        // × rides with the selected pill: it starts at the pill's original
        // slot center and glides to just right of the pill's final left-edge
        // position, driven by the SAME collapse fraction (perfectly in sync).
        // The overlay Box itself is transparent and lets clicks pass through.
        val selectedSlotCenterX = pitch * (selectedIndex.toFloat() + 0.5f)
        val collapsedX = pillW + gap + 18.dp
        val closeCenterX =
            selectedSlotCenterX * (1f - collapseFraction) + collapsedX * collapseFraction
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.TopStart
        ) {
            AnimatedVisibility(
                visible = collapsed,
                enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.6f, animationSpec = tween(250)),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.6f, animationSpec = tween(200)),
                modifier = Modifier.offset(x = closeCenterX - 18.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = { collapsed = false })
                ) {
                    Text(
                        text = "×",
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        color = ExploreNavy,
                        textAlign = TextAlign.Center
                    )
                }
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
private fun FeaturedCarousel(
    events: List<Event>,
    onOpen: () -> Unit
) {
    // Graceful degenerates: nothing for zero events, a static banner for one.
    if (events.isEmpty()) return
    if (events.size == 1) {
        Column {
            Spacer(modifier = Modifier.height(2.dp))
            FeaturedBanner(event = events[0], onOpen = onOpen)
        }
        return
    }
    // Virtual infinite loop over the real list: no duplicated data, no gaps.
    // The start page is centered so swiping works both ways immediately.
    val pageCount = Int.MAX_VALUE
    val startPage = (pageCount / 2) - ((pageCount / 2) % events.size)
    val pagerState = rememberPagerState(initialPage = startPage) { pageCount }
    // Lifecycle-safe timer: this coroutine is cancelled automatically when
    // the carousel leaves composition. It never fights an active swipe, and
    // advancing one adjacent page at a time keeps motion smooth with no jumps.
    LaunchedEffect(pagerState) {
        while (true) {
            delay(7500)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(
                        durationMillis = 1300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }
    Column {
        Spacer(modifier = Modifier.height(2.dp))
        // Pages are exactly 32dp narrower than the viewport, so the current
        // card stays fully readable while a 32dp edge of the next card peeks
        // in from the right on every screen width.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds(),
                pageSpacing = 8.dp,
                pageSize = PageSize.Fixed(maxWidth - 32.dp),
                key = { it }
            ) { index ->
                // Each page binds its own event, so title/date/location always
                // correspond to the currently displayed banner.
                FeaturedBanner(event = events[index % events.size], onOpen = onOpen)
            }
        }
    }
}

@Composable
private fun FeaturedBanner(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
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
                    .clip(RoundedCornerShape(10.dp))
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
                    fontSize = 15.sp,
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
private fun CommunityPostsLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Purple,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun CommunityPostsError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = ExploreMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text(
                text = "Retry",
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CommunityPostsEmpty(query: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (query.isBlank()) {
                "No community posts yet"
            } else {
                "No posts match “$query”"
            },
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = ExploreMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CommunityPostCard(
    post: Event,
    saved: Boolean,
    onBookmark: () -> Unit,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.title,
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                color = ExploreNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (post.description.isNotBlank()) {
                Text(
                    text = post.description,
                    fontFamily = AlumniSansSc,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = ExploreMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF0EDF9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = post.category,
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = Purple,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_place),
                    contentDescription = null,
                    tint = ExploreMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.location,
                    fontFamily = AlumniSansSc,
                    fontSize = 11.sp,
                    color = ExploreMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.time,
                fontFamily = AlumniSansSc,
                fontSize = 11.sp,
                color = ExploreMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onBookmark,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (saved) R.drawable.ic_bookmark
                        else R.drawable.ic_bookmark_outline
                ),
                contentDescription = if (saved) "Remove saved post" else "Save post",
                tint = if (saved) Purple else ExploreMuted,
                modifier = Modifier.size(22.dp)
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
