package com.bekaryab.qadamevents.data.repository

import com.bekaryab.qadamevents.domain.model.AuthUser
import com.bekaryab.qadamevents.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth =
        FirebaseAuth.getInstance()
) : AuthRepository {

    private val _authState =
        MutableStateFlow(firebaseAuth.currentUser?.toAuthUser())
    override val authState: StateFlow<AuthUser?> =
        _authState.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _authState.value = auth.currentUser?.toAuthUser()
        }
    }

    override suspend fun register(
        email: String,
        password: String
    ): Result<AuthUser> = safeCall {
        val result = firebaseAuth
            .createUserWithEmailAndPassword(
                email.trim(),
                password
            )
            .await()

        val user = result.user
            ?: error("Пайдаланушы құрылмады")

        user.toAuthUser()
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthUser> = safeCall {
        val result = firebaseAuth
            .signInWithEmailAndPassword(
                email.trim(),
                password
            )
            .await()

        val user = result.user
            ?: error("Пайдаланушы табылмады")

        user.toAuthUser()
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toAuthUser()
    }

    private suspend fun <T> safeCall(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email.orEmpty()
        )
    }
}