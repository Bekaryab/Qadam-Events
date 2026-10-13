package com.bekaryab.qadamevents.ui.events.list

import com.bekaryab.qadamevents.domain.model.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for the shared [filterEvents] logic (no Android framework,
 * no coroutines, no ViewModel instantiation).
 */
class EventsFilterTest {

    private fun event(
        id: String = "e",
        title: String = "KBTU Hackathon",
        description: String = "Code all night",
        category: String = "Hackathon",
        location: String = "Almaty, KBTU"
    ) = Event(
        id = id,
        title = title,
        location = location,
        time = "25 Oct 2026 · 10:00",
        dateBadge = "25 OCT",
        description = description,
        category = category
    )

    private val all = listOf(
        event(id = "1", title = "KBTU Hackathon", description = "Code all night", category = "Hackathon"),
        event(id = "2", title = "Design Meetup", description = "Pixels and prototypes", category = "Meetup"),
        event(id = "3", title = "AI Lecture", description = "Neural networks 101", category = "Lecture")
    )

    @Test
    fun blankQueryReturnsAll() {
        assertEquals(all, filterEvents(all, "", null))
        assertEquals(all, filterEvents(all, "   ", null))
    }

    @Test
    fun queryMatchesTitleCaseInsensitively() {
        assertEquals(listOf("1"), filterEvents(all, "hackathon", null).map { it.id })
        assertEquals(listOf("1"), filterEvents(all, "HACK", null).map { it.id })
    }

    @Test
    fun queryMatchesDescriptionCategoryAndLocation() {
        assertEquals(listOf("1"), filterEvents(all, "code all night", null).map { it.id })
        assertEquals(listOf("2"), filterEvents(all, "meetup", null).map { it.id })
        assertEquals(listOf("1", "2", "3"), filterEvents(all, "almaty", null).map { it.id })
    }

    @Test
    fun categoryFilterApplies() {
        assertEquals(listOf("2"), filterEvents(all, "", "Meetup").map { it.id })
    }

    @Test
    fun queryAndCategoryCombine() {
        assertTrue(filterEvents(all, "hackathon", "Meetup").isEmpty())
        assertEquals(listOf("1"), filterEvents(all, "k", "Hackathon").map { it.id })
    }

    @Test
    fun noMatchReturnsEmpty() {
        assertTrue(filterEvents(all, "zzz-no-such-event", null).isEmpty())
    }
}
