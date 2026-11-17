package com.bekaryab.qadamevents.domain.usecase

import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.RemoteEventRepository

/**
 * Retrieves the event list from the remote repository.
 *
 * Network and mapping exceptions propagate to the caller.
 */
class GetEventsUseCase(
    private val remoteRepository: RemoteEventRepository
) {

    suspend operator fun invoke(): List<Event> =
        remoteRepository.getEvents()
}
