package com.bekaryab.qadamevents.ui.events.list

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.usecase.GetEventsUseCase
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "EventsViewModel"
private const val KEY_QUERY = "events_search_query"
private const val KEY_CATEGORY = "events_selected_category"
private const val KEY_FAVORITES = "events_favorite_ids"

/**
 * Owns the Events screen state and loads events from the remote API through
 * [GetEventsUseCase]. The screen starts in [EventsUiState.Loading]; every
 * fetch either produces [EventsUiState.Success] or [EventsUiState.Error].
 *
 * Search, category, and favorites stay local (instant response) and are
 * restored from [savedStateHandle] after rotation/process recreation.
 */
class EventsViewModel(
    private val getEventsUseCase: GetEventsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow(
        savedStateHandle.get<String>(KEY_QUERY).orEmpty()
    )
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(
        savedStateHandle.get<String>(KEY_CATEGORY)
    )
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _favoriteEventIds = MutableStateFlow(
        savedStateHandle.get<ArrayList<String>>(KEY_FAVORITES)
            ?.toSet()
            .orEmpty()
    )
    val favoriteEventIds: StateFlow<Set<String>> = _favoriteEventIds.asStateFlow()

    private var allEvents: List<Event> = emptyList()

    init {
        Log.d(TAG, "init: restored query/category/favorites from SavedStateHandle")
    }

    /** Initial and manual load through the remote use case. */
    fun loadEvents() {
        fetch()
    }

    /** Error-state retry: shows Loading and requests the API again. */
    fun onRetry() {
        fetch()
    }

    private fun fetch() {
        _uiState.value = EventsUiState.Loading
        viewModelScope.launch {
            try {
                val events = getEventsUseCase()
                allEvents = events
                _uiState.value = EventsUiState.Success(
                    filterEvents(events, _searchQuery.value, _selectedCategory.value)
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = EventsUiState.Error(error.toReadableMessage())
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        savedStateHandle[KEY_QUERY] = query
        _searchQuery.value = query
        refreshVisibleEvents()
    }

    fun onCategorySelected(category: String?) {
        if (category == null) {
            savedStateHandle.remove<String>(KEY_CATEGORY)
        } else {
            savedStateHandle[KEY_CATEGORY] = category
        }
        _selectedCategory.value = category
        refreshVisibleEvents()
    }

    fun onFavoriteClick(eventId: String) {
        val updated = _favoriteEventIds.value.let { ids ->
            if (eventId in ids) ids - eventId else ids + eventId
        }
        savedStateHandle[KEY_FAVORITES] = ArrayList(updated)
        _favoriteEventIds.value = updated
    }

    /** Re-applies local filters only when data is already on screen. */
    private fun refreshVisibleEvents() {
        if (_uiState.value is EventsUiState.Success) {
            _uiState.value = EventsUiState.Success(
                filterEvents(allEvents, _searchQuery.value, _selectedCategory.value)
            )
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        super.onCleared()
    }
}

private fun Throwable.toReadableMessage(): String = when (this) {
    is IOException ->
        "Couldn't reach the server. Check your connection and try again."
    else -> message?.takeIf { it.isNotBlank() }
        ?: "Couldn't load events. Try again."
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
