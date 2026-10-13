package com.bekaryab.qadamevents.domain.usecase

import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.EventRepository

/** Searches stored + demo events by text through the repository. */
class SearchEventsUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(query: String): Result<List<Event>> =
        repository.searchEvents(query)
}
