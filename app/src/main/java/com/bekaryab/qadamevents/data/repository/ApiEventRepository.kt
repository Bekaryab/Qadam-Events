package com.bekaryab.qadamevents.data.repository

import com.bekaryab.qadamevents.data.mapper.toDomainEvent
import com.bekaryab.qadamevents.data.remote.PostApi
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.RemoteEventRepository

/**
 * Fetches DummyJSON posts and maps them to domain events.
 *
 * Network and mapping exceptions propagate to the caller so the ViewModel
 * can present the appropriate error state; cancellation is never swallowed.
 */
class ApiEventRepository(
    private val postApi: PostApi
) : RemoteEventRepository {

    override suspend fun getEvents(): List<Event> {
        val response = postApi.getPosts()
        val posts = response.posts
            ?: throw IllegalStateException("PostsResponseDto.posts is missing")
        return posts.map { it.toDomainEvent() }
    }
}
