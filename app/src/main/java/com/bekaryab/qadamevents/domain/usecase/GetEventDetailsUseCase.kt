package com.bekaryab.qadamevents.domain.usecase

import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.EventRepository

/** Retrieves a single event by id through the repository. */
class GetEventDetailsUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(id: String): Result<Event> =
        repository.getEventDetails(id)
}
