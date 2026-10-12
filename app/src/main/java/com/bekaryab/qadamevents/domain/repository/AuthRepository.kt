package com.bekaryab.qadamevents.domain.repository

import com.bekaryab.qadamevents.domain.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    suspend fun register(
        email: String,
        password: String
    ): Result<AuthUser>

    suspend fun login(
        email: String,
        password: String
    ): Result<AuthUser>

    fun logout()

    fun getCurrentUser(): AuthUser?

    /**
     * Firebase session as a lifecycle-safe flow. Emits the current user on
     * launch (including restored sessions), sign-in, sign-out, and session
     * invalidation. Firebase owns persistence; nothing is stored manually.
     */
    val authState: StateFlow<AuthUser?>
}