package com.bekaryab.qadamevents.data.local

import com.bekaryab.qadamevents.domain.model.Event

/**
 * TEMPORARY local test data shared by the Explore screen and the SIS 4 data
 * layer. Clearly separated from real data: nothing here comes from a server,
 * and these items never appear in Profile → My Events (that list is
 * owner-filtered repository data). Delete once a real event source exists.
 */
object SampleExploreEvents {

    val featured = Event(
        id = "demo-qadam-tech-conference",
        title = "Qadam Tech Conference",
        location = "Almaty, KBTU",
        time = "25 Oct 2026 · 09:00–18:00",
        dateBadge = "25 OCT",
        isUpcoming = true,
        description = "Flagship Qadam technology gathering.",
        category = "Conference",
        ownerUid = null
    )

    val popular: List<Event> = listOf(
        featured,
        Event(
            id = "demo-student-hackathon",
            title = "Student Hackathon",
            location = "Almaty, SmArt.Point",
            time = "12 Nov 2026 · 10:00–20:00",
            dateBadge = "12 NOV",
            isUpcoming = true,
            description = "48-hour student hackathon.",
            category = "Hackathon",
            ownerUid = null
        ),
        Event(
            id = "demo-design-meetup",
            title = "Design Meetup",
            location = "Almaty, Most Hub",
            time = "5 Nov 2026 · 18:00–20:00",
            dateBadge = "05 NOV",
            isUpcoming = true,
            description = "Monthly product design meetup.",
            category = "Meetup",
            ownerUid = null
        ),
        Event(
            id = "demo-mobile-dev-workshop",
            title = "Mobile Dev Workshop",
            location = "Astana Hub, Astana",
            time = "18 Nov 2026 · 12:00–15:00",
            dateBadge = "18 NOV",
            isUpcoming = true,
            description = "Hands-on Compose workshop.",
            category = "Workshop",
            ownerUid = null
        )
    )
}
