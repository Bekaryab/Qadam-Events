package com.bekaryab.qadamevents.core.network

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Builds a reusable Retrofit client for the REST API.
 *
 * The base URL is supplied by the caller, so no server address is hardcoded.
 */
object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Creates a Retrofit instance for [baseUrl].
     *
     * @throws IllegalArgumentException if the URL does not end with '/'.
     */
    fun create(baseUrl: String): Retrofit {
        val normalizedBaseUrl = baseUrl.trim()
        require(normalizedBaseUrl.endsWith("/")) {
            "Base URL must end with a trailing slash: $normalizedBaseUrl"
        }

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
