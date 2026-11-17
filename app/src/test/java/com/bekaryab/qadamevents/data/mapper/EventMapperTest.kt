package com.bekaryab.qadamevents.data.mapper

import com.bekaryab.qadamevents.data.model.PostDto
import com.bekaryab.qadamevents.data.model.PostReactionsDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/** Pure-JVM tests for [PostDto.toDomainEvent]. */
class EventMapperTest {

    @Test
    fun validPostMapsToDomainEvent() {
        val event = post().toDomainEvent()

        assertEquals("7", event.id)
        assertEquals("Example post title", event.title)
        assertEquals("Online · User #121", event.location)
        assertEquals("305 views · 192 likes", event.time)
        assertEquals("POST", event.dateBadge)
        assertEquals("Example post content", event.description)
        assertEquals("History", event.category)
        assertFalse(event.isUpcoming)
        assertFalse(event.isArchived)
    }

    @Test
    fun idIsConvertedToString() {
        assertEquals("42", post(id = 42).toDomainEvent().id)
    }

    @Test
    fun titleAndBodyAreMapped() {
        val event = post(title = " A post ", body = "Body").toDomainEvent()

        assertEquals("A post", event.title)
        assertEquals("Body", event.description)
        assertEquals("", post(body = null).toDomainEvent().description)
    }

    @Test
    fun firstTagBecomesCapitalizedCategory() {
        assertEquals(
            "History",
            post(tags = listOf("history", "fiction")).toDomainEvent().category
        )
        assertEquals("Sci-fi", post(tags = listOf("sci-fi")).toDomainEvent().category)
        assertEquals("Post", post(tags = emptyList()).toDomainEvent().category)
        assertEquals("Post", post(tags = null).toDomainEvent().category)
    }

    @Test
    fun viewsAndLikesAreFormattedAsPostStats() {
        assertEquals("305 views · 192 likes", post().toDomainEvent().time)
        assertEquals(
            "305 views",
            post(reactions = PostReactionsDto(likes = null, dislikes = 25))
                .toDomainEvent().time
        )
        assertEquals("192 likes", post(views = null).toDomainEvent().time)
        assertEquals("Post", post(views = null, reactions = null).toDomainEvent().time)
    }

    @Test
    fun missingOptionalValuesUseDefaults() {
        val event = post(
            body = null,
            tags = null,
            reactions = null,
            views = null,
            userId = null
        ).toDomainEvent()

        assertEquals("", event.description)
        assertEquals("Post", event.category)
        assertEquals("Post", event.time)
        assertEquals("Online post", event.location)
        assertNull(event.coverImageUri)
        assertFalse(event.isArchived)
    }

    @Test
    fun missingIdThrows() {
        assertThrows(IllegalArgumentException::class.java) {
            post(id = null).toDomainEvent()
        }
    }

    @Test
    fun invalidIdThrows() {
        assertThrows(IllegalArgumentException::class.java) {
            post(id = 0).toDomainEvent()
        }
    }

    @Test
    fun missingTitleThrows() {
        assertThrows(IllegalArgumentException::class.java) {
            post(title = null).toDomainEvent()
        }
        assertThrows(IllegalArgumentException::class.java) {
            post(title = " ").toDomainEvent()
        }
    }

    @Test
    fun isUpcomingIsAlwaysFalse() {
        assertFalse(post().toDomainEvent().isUpcoming)
    }

    @Test
    fun noFabricatedDatesOrOwnerUid() {
        val event = post().toDomainEvent()

        assertNull(event.startMillis)
        assertNull(event.endMillis)
        assertNull(event.ownerUid)
        assertNull(event.coverImageUri)
        assertEquals("POST", event.dateBadge)
        assertEquals("305 views · 192 likes", event.time)
    }

    private fun post(
        id: Int? = 7,
        title: String? = "Example post title",
        body: String? = "Example post content",
        tags: List<String>? = listOf("history", "fiction"),
        reactions: PostReactionsDto? = PostReactionsDto(likes = 192, dislikes = 25),
        views: Int? = 305,
        userId: Int? = 121
    ) = PostDto(
        id = id,
        title = title,
        body = body,
        tags = tags,
        reactions = reactions,
        views = views,
        userId = userId
    )
}
