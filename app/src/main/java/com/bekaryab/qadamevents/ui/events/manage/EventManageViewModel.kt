package com.bekaryab.qadamevents.ui.events.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.EventRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageUiState(
    val errorMessage: String? = null,
    val notice: String? = null
)

/**
 * Owns archive/restore/delete business operations. The repository owns the
 * data; this ViewModel only translates results into one-shot UI feedback.
 * Every mutating call verifies the caller's expected owner UID against the
 * stored event, so one user's actions can never touch another user's events
 * even if the UI ever passes a wrong id.
 */
class EventManageViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageUiState())
    val uiState = _uiState.asStateFlow()

    fun archiveEvent(id: String, expectedOwnerUid: String?) {
        mutate(id, expectedOwnerUid, "Event archived") { event ->
            event.copy(isArchived = true)
        }
    }

    fun restoreEvent(id: String, expectedOwnerUid: String?) {
        mutate(id, expectedOwnerUid, "Event restored") { event ->
            event.copy(isArchived = false)
        }
    }

    fun deleteEvent(id: String, expectedOwnerUid: String?) {
        viewModelScope.launch {
            try {
                val current = eventRepository.events.value
                    .firstOrNull { it.id == id }
                    ?: return@launch fail("Event not found")

                if (!isOwnedBy(current.ownerUid, expectedOwnerUid)) {
                    return@launch fail("You can only delete your own events")
                }

                eventRepository.deleteEvent(id).fold(
                    onSuccess = { notify("Event deleted") },
                    onFailure = { error -> fail(error.message ?: "Couldn't delete the event") }
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                fail("Couldn't delete the event")
            }
        }
    }

    fun consumeFeedback() {
        _uiState.update { ManageUiState() }
    }

    private fun mutate(
        id: String,
        expectedOwnerUid: String?,
        successNotice: String,
        transform: (Event) -> Event
    ) {
        viewModelScope.launch {
            try {
                val current = eventRepository.events.value
                    .firstOrNull { it.id == id }
                    ?: return@launch fail("Event not found")

                if (!isOwnedBy(current.ownerUid, expectedOwnerUid)) {
                    return@launch fail("You can only manage your own events")
                }

                eventRepository.updateEvent(transform(current)).fold(
                    onSuccess = { notify(successNotice) },
                    onFailure = { error -> fail(error.message ?: "Operation failed") }
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                fail("Operation failed")
            }
        }
    }

    private fun isOwnedBy(ownerUid: String?, expectedOwnerUid: String?): Boolean =
        ownerUid != null && ownerUid == expectedOwnerUid

    private fun notify(message: String) {
        _uiState.update { ManageUiState(notice = message) }
    }

    private fun fail(message: String) {
        _uiState.update { ManageUiState(errorMessage = message) }
    }
}
