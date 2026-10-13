package com.bekaryab.qadamevents.ui.events.list

import com.bekaryab.qadamevents.domain.model.Event

/**
 * Single immutable state for the SIS 4 Events screen. All properties are
 * `val`; every update goes through `copy()`. The four UI states derive from
 * [isLoading], [errorMessage], and [visibleEvents]:
 * Loading = isLoading; Error = !isLoading && errorMessage != null;
 * Empty = !isLoading && errorMessage == null && visibleEvents.isEmpty();
 * Content = otherwise.
 */
data class EventsUiState(
    val allEvents: List<Event> = emptyList(),
    val visibleEvents: List<Event> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val favoriteEventIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
