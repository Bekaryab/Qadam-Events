
package com.bekaryab.qadamevents.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekaryab.qadamevents.data.local.SampleExploreEvents
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.EventRepository
import com.bekaryab.qadamevents.domain.repository.RemoteEventRepository
import com.bekaryab.qadamevents.domain.repository.LocationRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** Feed tabs for the Explore category row. */
enum class ExploreFeed(val label: String) {
    FOR_YOU("For You"),
    EVENTS("Events"),
    PEOPLE("People"),
    PLACES("Places")
}

/** Interest tile; tapping one searches for its query. */
data class ExploreInterest(
    val label: String,
    val iconRes: Int,
    val background: androidx.compose.ui.graphics.Color,
    val query: String
)

class ExploreViewModel(
    private val eventRepository: EventRepository,
    private val remoteEventRepository: RemoteEventRepository,
    private val locationRepository: LocationRepository? = null
) : ViewModel() {

    // Search state
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Selected feed
    private val _feed = MutableStateFlow(ExploreFeed.FOR_YOU)
    val feed: StateFlow<ExploreFeed> = _feed.asStateFlow()

    // Upcoming events filter
    private val _upcomingOnly = MutableStateFlow(false)
    val upcomingOnly: StateFlow<Boolean> = _upcomingOnly.asStateFlow()

    // Category filter
    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category.asStateFlow()

    // Bookmarks
    private val _bookmarkedIds = MutableStateFlow<List<String>>(emptyList())
    val bookmarkedIds: StateFlow<List<String>> = _bookmarkedIds.asStateFlow()

    // Location state
    private val _locationUiState =
        MutableStateFlow<ExploreLocationUiState>(
            ExploreLocationUiState.Idle
        )

    val locationUiState: StateFlow<ExploreLocationUiState> =
        _locationUiState.asStateFlow()

    // Community posts state
    private val _communityPosts =
        MutableStateFlow<CommunityPostsUiState>(CommunityPostsUiState.Loading)

    val communityPosts: StateFlow<CommunityPostsUiState> =
        _communityPosts.asStateFlow()

    private var allCommunityPosts: List<Event> = emptyList()
    private var communityPostsRequested = false

    /**
     * Visible events.
     *
     * Combines:
     * - User events
     * - Search query
     * - Feed selection
     * - Upcoming filter
     * - Category filter
     * - Current city
     *
     * When a city is detected, events are filtered by location.
     */
    val popular: StateFlow<List<Event>> = combine(
        combine(
            eventRepository.events,
            _query,
            _feed,
            _upcomingOnly,
            _category,
            ::filterPopular
        ),
        _locationUiState
    ) { events, locationState ->

        val city =
            (locationState as? ExploreLocationUiState.Success)?.city

        if (city.isNullOrBlank()) {
            events
        } else {
            events.filter {
                it.location.contains(city, ignoreCase = true)
            }
        }

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    /**
     * Filters events by search, feed, category and upcoming status.
     */
    private fun filterPopular(
        userEvents: List<Event>,
        query: String,
        feed: ExploreFeed,
        upcomingOnly: Boolean,
        category: String?
    ): List<Event> {

        val activeUserEvents = userEvents.filterNot {
            it.isArchived
        }

        val base: List<Event> = when (feed) {
            ExploreFeed.FOR_YOU ->
                activeUserEvents + SampleExploreEvents.popular

            ExploreFeed.EVENTS ->
                activeUserEvents

            ExploreFeed.PEOPLE,
            ExploreFeed.PLACES ->
                emptyList()
        }

        val searched = if (query.isBlank()) {
            base
        } else {
            base.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true)
            }
        }

        val categorized = if (category == null) {
            searched
        } else {
            searched.filter {
                it.category == category
            }
        }

        return if (upcomingOnly) {
            categorized.filter { it.isUpcoming }
        } else {
            categorized
        }
    }

    /**
     * Requests the current city from LocationRepository.
     */
    fun requestCurrentCity() {

        if (_locationUiState.value is ExploreLocationUiState.Loading) {
            return
        }

        viewModelScope.launch {

            _locationUiState.value =
                ExploreLocationUiState.Loading

            val repository = locationRepository

            if (repository == null) {
                _locationUiState.value =
                    ExploreLocationUiState.Error(
                        "Location repository is not configured"
                    )
                return@launch
            }

            try {

                repository.getCurrentCity().fold(

                    onSuccess = { city ->

                        _locationUiState.value =
                            if (city.isNotBlank()) {
                                ExploreLocationUiState.Success(city)
                            } else {
                                ExploreLocationUiState.Error(
                                    "City was not found"
                                )
                            }
                    },

                    onFailure = { error ->

                        _locationUiState.value =
                            ExploreLocationUiState.Error(
                                when (error) {
                                    is SecurityException ->
                                        "Location permission was not granted"

                                    else ->
                                        error.message
                                            ?: "Couldn't determine your city"
                                }
                            )
                    }
                )

            } catch (cancelled: CancellationException) {

                throw cancelled

            } catch (error: Exception) {

                _locationUiState.value =
                    ExploreLocationUiState.Error(
                        error.message
                            ?: "Couldn't determine your city"
                    )
            }
        }
    }

    /**
     * Removes the city filter.
     */
    fun clearLocationFilter() {
        _locationUiState.value =
            ExploreLocationUiState.Idle
    }

    /**
     * Loads community posts for the first time.
     */
    fun loadCommunityPosts() {
        if (communityPostsRequested) return

        communityPostsRequested = true
        fetchCommunityPosts()
    }

    /**
     * Retries loading community posts.
     */
    fun retryCommunityPosts() {
        communityPostsRequested = true
        fetchCommunityPosts()
    }

    /**
     * Fetches community posts from remote API.
     */
    private fun fetchCommunityPosts() {

        _communityPosts.value =
            CommunityPostsUiState.Loading

        viewModelScope.launch {

            try {

                val posts = remoteEventRepository.getEvents()

                allCommunityPosts = posts

                _communityPosts.value =
                    CommunityPostsUiState.Success(
                        filterCommunityPosts(posts, _query.value)
                    )

            } catch (cancelled: CancellationException) {

                throw cancelled

            } catch (_: IOException) {

                _communityPosts.value =
                    CommunityPostsUiState.Error(
                        "Couldn't reach the server. Check your connection and try again."
                    )

            } catch (error: HttpException) {

                _communityPosts.value =
                    CommunityPostsUiState.Error(
                        "Server error (${error.code()}). Please try again."
                    )

            } catch (error: Exception) {

                _communityPosts.value =
                    CommunityPostsUiState.Error(
                        error.message?.takeIf { it.isNotBlank() }
                            ?: "Couldn't load community posts. Try again."
                    )
            }
        }
    }

    /**
     * Filters community posts by search query.
     */
    private fun filterCommunityPosts(
        posts: List<Event>,
        query: String
    ): List<Event> {

        if (query.isBlank()) return posts

        return posts.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.location.contains(query, ignoreCase = true)
        }
    }

    private fun refreshCommunityPosts() {

        val current = _communityPosts.value

        if (current is CommunityPostsUiState.Success) {

            _communityPosts.value =
                CommunityPostsUiState.Success(
                    filterCommunityPosts(
                        allCommunityPosts,
                        _query.value
                    )
                )
        }
    }

    fun setQuery(value: String) {
        _query.update { value }
        refreshCommunityPosts()
    }

    fun selectFeed(feed: ExploreFeed) {
        _feed.update { feed }
    }

    fun setUpcomingOnly(enabled: Boolean) {
        _upcomingOnly.update { enabled }
    }

    fun setCategory(category: String?) {
        _category.update { category }
    }

    /**
     * Resets every filter, including the search query.
     */
    fun clearFilters() {
        _category.update { null }
        _upcomingOnly.update { false }
        _query.update { "" }
        refreshCommunityPosts()
    }

    fun toggleBookmark(id: String) {
        _bookmarkedIds.update { ids ->
            if (id in ids) ids - id else ids + id
        }
    }
}
