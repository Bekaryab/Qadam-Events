package com.bekaryab.qadamevents.ui.events.list

import com.bekaryab.qadamevents.domain.model.Event

/**
 * Screen state for the Events feature.
 *
 * [Success.events] is the list after local search/category filtering;
 * the unfiltered list stays inside the ViewModel.
 */
sealed interface EventsUiState {

    /** Initial and refetch state. */
    data object Loading : EventsUiState

    /** Events ready to render (possibly empty). */
    data class Success(val events: List<Event>) : EventsUiState

    /** Request failed; [message] is user-readable. */
    data class Error(val message: String) : EventsUiState
}
