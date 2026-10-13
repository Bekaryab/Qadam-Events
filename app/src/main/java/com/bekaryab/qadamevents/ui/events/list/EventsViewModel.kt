package com.bekaryab.qadamevents.ui.events.list

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.usecase.GetEventsUseCase
import com.bekaryab.qadamevents.domain.usecase.SearchEventsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "EventsViewModel"
private const val KEY_QUERY = "events_search_query"
private const val KEY_CATEGORY = "events_selected_category"
private const val KEY_FAVORITES = "events_favorite_ids"

/**
 * SIS 4 Events ViewModel. Owns screen state; the repository owns data.
 * Search/filter/favorites are applied locally for instant response over the
 * loaded list; [SearchEventsUseCase] is kept as the seam for a future
 * server-side search. Not connected to navigation yet (next stage).
 *
 * Rotation: query, category, and favorites restore from [savedStateHandle];
 * the event list itself is reloaded via [loadEvents] (never stored in saved
 * state) and always replaces — never appends — so reloads can't duplicate.
 */
class EventsViewModel(
    private val getEventsUseCase: GetEventsUseCase,
    private val searchEventsUseCase: SearchEventsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EventsUiState(
            searchQuery = savedStateHandle.get<String>(KEY_QUERY).orEmpty(),
            selectedCategory = savedStateHandle.get<String>(KEY_CATEGORY),
            favoriteEventIds = savedStateHandle.get<ArrayList<String>>(KEY_FAVORITES)
                ?.toSet()
                .orEmpty()
        )
    )
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "init: restored query/category/favorites from SavedStateHandle")
    }

    /** Initial and manual load through the use case. */
    fun loadEvents() {
        fetch()
    }

    /** Error-state retry: clears the error, shows Loading, refetches. */
    fun onRetry() {
        fetch()
    }

    private fun fetch() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            // Result-based API: failures arrive as values and cancellation
            // propagates naturally — nothing is caught or swallowed here.
            getEventsUseCase().fold(
                onSuccess = { events ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            allEvents = events,
                            visibleEvents = filterEvents(
                                events,
                                current.searchQuery,
                                current.selectedCategory
                            ),
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        // Previously loaded data is kept; only the error shows.
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message.orEmpty()
                                .ifBlank { "Couldn't load events. Try again." }
                        )
                    }
                }
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        savedStateHandle[KEY_QUERY] = query
        _uiState.update {
            it.copy(
                searchQuery = query,
                visibleEvents = filterEvents(it.allEvents, query, it.selectedCategory)
            )
        }
    }

    fun onCategorySelected(category: String?) {
        if (category == null) {
            savedStateHandle.remove<String>(KEY_CATEGORY)
        } else {
            savedStateHandle[KEY_CATEGORY] = category
        }
        _uiState.update {
            it.copy(
                selectedCategory = category,
                visibleEvents = filterEvents(it.allEvents, it.searchQuery, category)
            )
        }
    }

    fun onFavoriteClick(eventId: String) {
        val updated = _uiState.value.favoriteEventIds.let { ids ->
            if (eventId in ids) ids - eventId else ids + eventId
        }
        savedStateHandle[KEY_FAVORITES] = ArrayList(updated)
        _uiState.update { it.copy(favoriteEventIds = updated) }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        super.onCleared()
    }
}

/**
 * Pure domain filter (no Android APIs): case-insensitive match on title,
 * description, category, and location, then category equality. Shared by the
 * ViewModel and plain-JVM unit tests.
 */
internal fun filterEvents(
    allEvents: List<Event>,
    query: String,
    category: String?
): List<Event> {
    val searched = if (query.isBlank()) {
        allEvents
    } else {
        allEvents.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true)
        }
    }
    return if (category == null) {
        searched
    } else {
        searched.filter { it.category == category }
    }
}
