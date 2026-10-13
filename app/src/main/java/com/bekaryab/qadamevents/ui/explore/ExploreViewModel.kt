package com.bekaryab.qadamevents.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekaryab.qadamevents.data.local.SampleExploreEvents
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

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
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _feed = MutableStateFlow(ExploreFeed.FOR_YOU)
    val feed: StateFlow<ExploreFeed> = _feed.asStateFlow()

    private val _upcomingOnly = MutableStateFlow(false)
    val upcomingOnly: StateFlow<Boolean> = _upcomingOnly.asStateFlow()

    // Null = All categories. Every event carries a category (user form has no
    // empty option; demos are tagged), so this filter is fully supported.
    // Date-range and location-directory filters are omitted: neither user nor
    // demo data provides the backing values.
    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category.asStateFlow()

    private val _bookmarkedIds = MutableStateFlow<List<String>>(emptyList())
    val bookmarkedIds: StateFlow<List<String>> = _bookmarkedIds.asStateFlow()

    /**
     * Visible Popular rows. Combines user-created repository events with the
     * clearly-marked demo list; search, category, and the upcoming toggle
     * filter both. People/Places have no directory yet, so those feeds stay
     * empty regardless of filters.
     */
    val popular: StateFlow<List<Event>> = combine(
        eventRepository.events,
        _query,
        _feed,
        _upcomingOnly,
        _category,
        ::filterPopular
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun filterPopular(
        userEvents: List<Event>,
        query: String,
        feed: ExploreFeed,
        upcomingOnly: Boolean,
        category: String?
    ): List<Event> {
        val base: List<Event> = when (feed) {
            ExploreFeed.FOR_YOU -> userEvents + SampleExploreEvents.popular
            ExploreFeed.EVENTS -> userEvents
            ExploreFeed.PEOPLE, ExploreFeed.PLACES -> emptyList()
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
            searched.filter { it.category == category }
        }
        return if (upcomingOnly) {
            categorized.filter { it.isUpcoming }
        } else {
            categorized
        }
    }

    fun setQuery(value: String) {
        _query.update { value }
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

    /** Resets every filter, including the search query. */
    fun clearFilters() {
        _category.update { null }
        _upcomingOnly.update { false }
        _query.update { "" }
    }

    fun toggleBookmark(id: String) {
        _bookmarkedIds.update { ids -> if (id in ids) ids - id else ids + id }
    }
}
