package com.bekaryab.qadamevents.domain.repository

import com.bekaryab.qadamevents.domain.model.Event
import kotlinx.coroutines.flow.StateFlow

interface EventRepository {

    /** All stored events as an observable list. */
    val events: StateFlow<List<Event>>

    /** Stores a new event; fails if the id already exists (no duplicates). */
    suspend fun addEvent(event: Event): Result<Event>

    /**
     * Simulation control for the SIS 4 data layer only. When true, the
     * simulated read operations below fail instead of returning data.
     * Defaults to false, so production paths are unaffected.
     */
    var shouldFail: Boolean

    /**
     * Simulated remote read: suspends ~1.5s, then returns stored user events
     * plus local test data (or a failure when [shouldFail] is set). Used only
     * by the SIS 4 Events flow; the instant [events] flow above is untouched.
     */
    suspend fun getEvents(): Result<List<Event>>

    /** Simulated remote search over title, location, description, category. */
    suspend fun searchEvents(query: String): Result<List<Event>>

    /** Simulated remote lookup; fails when no event has the given id. */
    suspend fun getEventDetails(id: String): Result<Event>
}
