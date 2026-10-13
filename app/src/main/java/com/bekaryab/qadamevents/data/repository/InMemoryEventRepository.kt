package com.bekaryab.qadamevents.data.repository

import com.bekaryab.qadamevents.data.local.SampleExploreEvents
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.EventRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared in-memory event store, held as a singleton in [com.bekaryab.qadamevents.di.AppModule].
 *
 * LIMITATION: no database or server exists yet, so events survive navigation
 * and configuration changes but NOT an app process restart. Replace this class
 * with a persistent implementation when a backend lands; the contract already
 * supports it ([addEvent] is suspending and result-typed).
 */
class InMemoryEventRepository : EventRepository {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    override val events: StateFlow<List<Event>> = _events.asStateFlow()

    override suspend fun addEvent(event: Event): Result<Event> {
        if (_events.value.any { it.id == event.id }) {
            return Result.failure(IllegalStateException("Event already saved"))
        }
        _events.value = _events.value + event
        return Result.success(event)
    }

    override var shouldFail: Boolean = false

    override suspend fun getEvents(): Result<List<Event>> = simulated {
        storedPlusDemo()
    }

    override suspend fun searchEvents(query: String): Result<List<Event>> =
        simulated {
            val all = storedPlusDemo()
            if (query.isBlank()) {
                all
            } else {
                all.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
                }
            }
        }

    override suspend fun getEventDetails(id: String): Result<Event> =
        simulated {
            storedPlusDemo().firstOrNull { it.id == id }
                ?: throw NoSuchElementException("Event not found")
        }

    private fun storedPlusDemo(): List<Event> =
        _events.value + SampleExploreEvents.popular

    /**
     * Simulated server round-trip shared by the read operations above. The
     * delay runs outside any try block so [CancellationException] from a
     * cancelled caller propagates instead of becoming a failure result.
     */
    private suspend fun <T> simulated(block: () -> T): Result<T> {
        delay(1500)
        if (shouldFail) {
            return Result.failure(IOException("Simulated network failure"))
        }
        return try {
            Result.success(block())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            Result.failure(failure)
        }
    }
}
