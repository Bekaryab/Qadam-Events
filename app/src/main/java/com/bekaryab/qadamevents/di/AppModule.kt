package com.bekaryab.qadamevents.di

import com.bekaryab.qadamevents.BuildConfig
import com.bekaryab.qadamevents.core.network.RetrofitClient
import com.bekaryab.qadamevents.data.remote.PostApi
import com.bekaryab.qadamevents.data.repository.ApiEventRepository
import com.bekaryab.qadamevents.data.repository.AuthRepositoryImpl
import com.bekaryab.qadamevents.data.repository.InMemoryEventRepository
import com.bekaryab.qadamevents.domain.repository.AuthRepository
import com.bekaryab.qadamevents.domain.repository.EventRepository
import com.bekaryab.qadamevents.domain.repository.RemoteEventRepository
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

    // Singleton: the list (and its StateFlow) survives navigation and
    // configuration changes. In-memory only — see InMemoryEventRepository.
    val eventRepository: EventRepository by lazy {
        InMemoryEventRepository()
    }

    // Remote API repository. The base URL comes from BuildConfig, so it can be
    // changed with -PEVENTS_BASE_URL. The default is the public DummyJSON
    // demo API. The "posts" path is defined by PostApi.
    val remoteEventRepository: RemoteEventRepository by lazy {
        val retrofit = RetrofitClient.create(BuildConfig.EVENTS_BASE_URL)
        ApiEventRepository(retrofit.create(PostApi::class.java))
    }
}
