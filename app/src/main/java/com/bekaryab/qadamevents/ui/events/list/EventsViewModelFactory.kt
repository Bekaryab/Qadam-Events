package com.bekaryab.qadamevents.ui.events.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.bekaryab.qadamevents.domain.repository.EventRepository
import com.bekaryab.qadamevents.domain.usecase.GetEventsUseCase
import com.bekaryab.qadamevents.domain.usecase.SearchEventsUseCase

/**
 * Factory for [EventsViewModel] only (mirrors [com.bekaryab.qadamevents.ui.auth.AuthViewModelFactory]).
 * The [SavedStateHandle] comes from the Activity's [CreationExtras], so the
 * ViewModel obtained through [ViewModelProvider] survives rotation in the
 * Activity's ViewModelStore.
 */
class EventsViewModelFactory(
    private val eventRepository: EventRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            return EventsViewModel(
                GetEventsUseCase(eventRepository),
                SearchEventsUseCase(eventRepository),
                extras.createSavedStateHandle()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
