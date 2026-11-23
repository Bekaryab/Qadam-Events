package com.bekaryab.qadamevents.data.remote

import com.bekaryab.qadamevents.data.model.PostsResponseDto
import retrofit2.http.GET

/**
 * Retrofit definition for the DummyJSON posts endpoint.
 *
 * Resolved against the base URL supplied to the Koin Retrofit definition, so with
 * https://dummyjson.com/ this call requests GET https://dummyjson.com/posts.
 */
interface PostApi {

    @GET("posts")
    suspend fun getPosts(): PostsResponseDto
}
