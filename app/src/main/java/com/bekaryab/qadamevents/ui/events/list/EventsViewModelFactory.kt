package com.bekaryab.qadamevents.ui.events.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.bekaryab.qadamevents.domain.repository.RemoteEventRepository
import com.bekaryab.qadamevents.domain.usecase.GetEventsUseCase

/**
 * Factory for [EventsViewModel] only (mirrors
 * [com.bekaryab.qadamevents.ui.auth.AuthViewModelFactory]).
 *
 * The [SavedStateHandle] comes from the Activity [CreationExtras], so the
 * ViewModel obtained through [ViewModelProvider] survives rotation in the
 * Activity ViewModelStore.
 */
class EventsViewModelFactory(
    private val remoteEventRepository: RemoteEventRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            return EventsViewModel(
                GetEventsUseCase(remoteEventRepository),
                extras.createSavedStateHandle()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
