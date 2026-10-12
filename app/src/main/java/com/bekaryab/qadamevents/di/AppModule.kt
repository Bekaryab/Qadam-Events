package com.bekaryab.qadamevents.di

import com.bekaryab.qadamevents.data.repository.AuthRepositoryImpl
import com.bekaryab.qadamevents.domain.repository.AuthRepository
import com.bekaryab.qadamevents.ui.auth.AuthViewModelFactory
import com.google.firebase.auth.FirebaseAuth

object AppModule {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(firebaseAuth)
    }

    val authViewModelFactory: AuthViewModelFactory by lazy {
        AuthViewModelFactory(authRepository)
    }
}