package com.bekaryab.qadamevents.domain.usecase

import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.EventRepository

/** Retrieves the event list through the repository (simulated latency inside). */
class GetEventsUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(): Result<List<Event>> =
        repository.getEvents()
}
