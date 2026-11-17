package com.bekaryab.qadamevents.data.model

/**
 * Wire representation of a DummyJSON post.
 *
 * All fields are nullable because the JSON may omit them; Gson does not
 * apply Kotlin constructor defaults, so nullability is the safe contract.
 */
data class PostDto(
    val id: Int? = null,
    val title: String? = null,
    val body: String? = null,
    val tags: List<String>? = null,
    val reactions: PostReactionsDto? = null,
    val views: Int? = null,
    val userId: Int? = null
)
