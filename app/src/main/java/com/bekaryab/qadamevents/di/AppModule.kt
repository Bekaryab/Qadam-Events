package com.bekaryab.qadamevents.di

import androidx.lifecycle.SavedStateHandle
import com.bekaryab.qadamevents.data.location.DeviceLocationRepository
import com.bekaryab.qadamevents.data.repository.ApiEventRepository
import com.bekaryab.qadamevents.data.repository.AuthRepositoryImpl
import com.bekaryab.qadamevents.data.repository.InMemoryEventRepository
import com.bekaryab.qadamevents.domain.repository.AuthRepository
import com.bekaryab.qadamevents.domain.repository.EventRepository
import com.bekaryab.qadamevents.domain.repository.LocationRepository
import com.bekaryab.qadamevents.domain.repository.RemoteEventRepository
import com.bekaryab.qadamevents.domain.usecase.GetEventDetailsUseCase
import com.bekaryab.qadamevents.domain.usecase.GetEventsUseCase
import com.bekaryab.qadamevents.domain.usecase.SearchEventsUseCase
import com.bekaryab.qadamevents.ui.auth.LoginViewModel
import com.bekaryab.qadamevents.ui.auth.RegisterViewModel
import com.bekaryab.qadamevents.ui.events.create.CreateEventViewModel
import com.bekaryab.qadamevents.ui.events.list.EventsViewModel
import com.bekaryab.qadamevents.ui.events.manage.EventManageViewModel
import com.bekaryab.qadamevents.ui.explore.ExploreViewModel
import com.bekaryab.qadamevents.ui.reels.ReelsViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }

    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::InMemoryEventRepository) bind EventRepository::class
    singleOf(::ApiEventRepository) bind RemoteEventRepository::class
    singleOf(::DeviceLocationRepository) bind LocationRepository::class

    singleOf(::GetEventsUseCase)
    singleOf(::SearchEventsUseCase)
    singleOf(::GetEventDetailsUseCase)

    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { EventsViewModel(get(), get<SavedStateHandle>()) }
    viewModel { CreateEventViewModel(get(), get()) }
    viewModel { ExploreViewModel(get(), get(), get()) }
    viewModel { EventManageViewModel(get()) }
    viewModel { ReelsViewModel() }
}
