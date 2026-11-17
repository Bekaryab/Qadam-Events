package com.bekaryab.qadamevents.data.mapper

import com.bekaryab.qadamevents.data.model.PostDto
import com.bekaryab.qadamevents.domain.model.Event

/**
 * Maps a DummyJSON [PostDto] to the domain [Event] so the existing Events
 * screen can render community posts with no separate UI stack.
 *
 * DummyJSON provides no event dates or Firebase users, so date fields stay
 * null, ownerUid is always null, and post statistics are shown in [Event.time]
 * without pretending to be event times.
 */
fun PostDto.toDomainEvent(): Event {
    val postId = requireNotNull(id) { "PostDto.id is required" }
    require(postId > 0) { "PostDto.id must be a positive integer: $postId" }
    val postTitle = title?.trim()?.takeIf { it.isNotEmpty() }
        ?: throw IllegalArgumentException("PostDto.title is required")

    val category = tags
        ?.firstOrNull()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.replaceFirstChar { it.uppercaseChar() }
        ?: "Post"

    val location = userId
        ?.takeIf { it > 0 }
        ?.let { "Online · User #$it" }
        ?: "Online post"

    val stats = listOfNotNull(
        views?.let { "$it views" },
        reactions?.likes?.let { "$it likes" }
    ).joinToString(" · ").ifEmpty { "Post" }

    return Event(
        id = postId.toString(),
        title = postTitle,
        location = location,
        time = stats,
        dateBadge = "POST",
        isUpcoming = false,
        description = body.orEmpty(),
        category = category,
        coverImageUri = null,
        ownerUid = null,
        startMillis = null,
        endMillis = null,
        isArchived = false
    )
}
