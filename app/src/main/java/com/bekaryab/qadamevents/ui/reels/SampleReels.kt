package com.bekaryab.qadamevents.ui.reels

import com.bekaryab.qadamevents.domain.model.Event

/** Feed selector options. */
enum class ReelFeed(val label: String) {
    FOR_YOU("For You"),
    EVENTS("Events"),
    PEOPLE("People")
}

/** Local demo reel. All values below are placeholder content, not backend data. */
data class ReelDemo(
    val id: String,
    val creatorName: String,
    val timeLocation: String,
    val title: String,
    val caption: String,
    val baseLikes: Int,
    val comments: Int,
    val shares: Int,
    val event: Event
)

/** Clearly separated temporary demo source. Delete once a reels feed exists. */
object SampleReels {

    private val forYou = ReelDemo(
        id = "reel-hackathon",
        creatorName = "Aigerim",
        timeLocation = "2h ago · Almaty, KBTU",
        title = "KBTU Hackathon 2026",
        caption = "Incredible people, ideas and atmosphere 💙",
        baseLikes = 1200,
        comments = 84,
        shares = 128,
        event = Event(
            id = "kbtu-hackaton-2026",
            title = "KBTU Hackathon 2026",
            location = "Almaty, KBTU",
            time = "25 Oct 2026 · 10:00–18:00",
            dateBadge = "25 OCT"
        )
    )

    private val events = ReelDemo(
        id = "reel-startup-weekend",
        creatorName = "Dias",
        timeLocation = "5h ago · Almaty, Smart Point",
        title = "Almaty Startup Weekend",
        caption = "48 hours, 12 teams, one unforgettable demo night",
        baseLikes = 860,
        comments = 41,
        shares = 57,
        event = Event(
            id = "startup-weekend-almaty",
            title = "Almaty Startup Weekend",
            location = "Smart Point, Almaty",
            time = "31 Oct 2026 · 18:30–21:00",
            dateBadge = "31 OCT"
        )
    )

    private val people = ReelDemo(
        id = "reel-ai-dialogue",
        creatorName = "Aruzhan",
        timeLocation = "1d ago · Astana, NU",
        title = "Dialogue: AI in Education",
        caption = "Students asking the questions that matter most",
        baseLikes = 640,
        comments = 29,
        shares = 33,
        event = Event(
            id = "ai-in-education",
            title = "Dialogue: AI in Education",
            location = "NU, Astana",
            time = "01 Nov 2026 · 11:00–13:00",
            dateBadge = "01 NOV"
        )
    )

    fun byFeed(feed: ReelFeed): ReelDemo = when (feed) {
        ReelFeed.FOR_YOU -> forYou
        ReelFeed.EVENTS -> events
        ReelFeed.PEOPLE -> people
    }
}
