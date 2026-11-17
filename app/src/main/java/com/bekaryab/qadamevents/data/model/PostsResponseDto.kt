package com.bekaryab.qadamevents.data.model

/** Paginated wrapper returned by GET https://dummyjson.com/posts. */
data class PostsResponseDto(
    val posts: List<PostDto>? = null,
    val total: Int? = null,
    val skip: Int? = null,
    val limit: Int? = null
)
