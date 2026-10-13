package com.bekaryab.qadamevents.ui.events.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.AuthRepository
import com.bekaryab.qadamevents.domain.repository.EventRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val category: String = "Other",
    val location: String = "",
    val dateMillis: Long? = null,
    val startMinutes: Int? = null,
    val endMinutes: Int? = null,
    val coverUri: String? = null,
    val validationAttempted: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

/** Categories offered by the Create form. */
val EventCategories = listOf(
    "Hackathon",
    "Conference",
    "Workshop",
    "Meetup",
    "Lecture",
    "Other"
)

class CreateEventViewModel(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState = _uiState.asStateFlow()

    /** Read-only organizer line, taken from the signed-in user (never re-typed). */
    val organizerEmail: String
        get() = authRepository.getCurrentUser()?.email.orEmpty()

    fun onTitleChange(value: String) =
        _uiState.update { it.copy(title = value, errorMessage = null) }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value, errorMessage = null) }

    fun onCategoryChange(value: String) =
        _uiState.update { it.copy(category = value, errorMessage = null) }

    fun onLocationChange(value: String) =
        _uiState.update { it.copy(location = value, errorMessage = null) }

    fun onDatePicked(millis: Long?) =
        _uiState.update {
            it.copy(dateMillis = millis?.let(::toLocalStartOfDay), errorMessage = null)
        }

    fun onStartPicked(minutes: Int) =
        _uiState.update { it.copy(startMinutes = minutes, errorMessage = null) }

    fun onEndPicked(minutes: Int) =
        _uiState.update { it.copy(endMinutes = minutes, errorMessage = null) }

    fun onCoverPicked(uri: String?) =
        _uiState.update { it.copy(coverUri = uri, errorMessage = null) }

    // Per-field messages; shown only after a save attempt. All rules live
    // here so Composables stay stateless.
    fun titleError(): String? =
        _uiState.value.takeIf { it.validationAttempted && it.title.isBlank() }
            ?.let { "Enter an event title" }

    fun descriptionError(): String? =
        _uiState.value.takeIf { it.validationAttempted && it.description.isBlank() }
            ?.let { "Enter a description" }

    fun locationError(): String? =
        _uiState.value.takeIf { it.validationAttempted && it.location.isBlank() }
            ?.let { "Enter a location" }

    fun dateError(): String? =
        _uiState.value.takeIf { it.validationAttempted && it.dateMillis == null }
            ?.let { "Pick a date" }

    fun startError(): String? =
        _uiState.value.takeIf { it.validationAttempted && it.startMinutes == null }
            ?.let { "Pick a start time" }

    fun endError(): String? =
        _uiState.value.takeIf { it.validationAttempted && it.endMinutes == null }
            ?.let { "Pick an end time" }

    fun save() {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(validationAttempted = true, errorMessage = null) }

        val firstError = titleError()
            ?: descriptionError()
            ?: locationError()
            ?: dateError()
            ?: startError()
            ?: endError()
            ?: endAfterStartError()
        if (firstError != null) {
            _uiState.update { it.copy(errorMessage = firstError) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val event = buildEvent(_uiState.value)
                val result = eventRepository.addEvent(event)
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = error.message
                                    ?: "Couldn't save the event. Try again."
                            )
                        }
                    }
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Couldn't save the event. Try again."
                    )
                }
            }
        }
    }

    /** Clears the one-shot success flag after navigation consumes it. */
    fun consumeSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun endAfterStartError(): String? {
        val state = _uiState.value
        val start = state.startMinutes
        val end = state.endMinutes
        return if (start != null && end != null && end <= start) {
            "End time must be after start time"
        } else {
            null
        }
    }

    private fun buildEvent(state: CreateEventUiState): Event {
        val dateMillis = requireNotNull(state.dateMillis)
        val start = requireNotNull(state.startMinutes)
        val end = requireNotNull(state.endMinutes)
        val dateText = dateFormat.format(Date(dateMillis))
        return Event(
            id = UUID.randomUUID().toString(),
            title = state.title.trim(),
            location = state.location.trim(),
            time = "$dateText · ${formatMinutes(start)}–${formatMinutes(end)}",
            dateBadge = badgeFormat.format(Date(dateMillis)).uppercase(Locale.US),
            isUpcoming = dateMillis >= startOfTodayMillis(),
            description = state.description.trim(),
            category = state.category,
            coverImageUri = state.coverUri,
            ownerUid = authRepository.getCurrentUser()?.uid,
            startMillis = dateMillis + start * 60_000L,
            endMillis = dateMillis + end * 60_000L
        )
    }

    companion object {
        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        private val badgeFormat = SimpleDateFormat("dd MMM", Locale.US)

        fun formatMinutes(minutes: Int): String =
            "%02d:%02d".format(minutes / 60, minutes % 60)

        fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

        /**
         * DatePicker returns UTC midnight; normalize to local start-of-day so
         * display strings, upcoming checks, and start/end instants are correct
         * in every timezone.
         */
        fun toLocalStartOfDay(utcMidnight: Long): Long {
            val picked = Calendar.getInstance().apply { timeInMillis = utcMidnight }
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, picked.get(Calendar.YEAR))
                set(Calendar.MONTH, picked.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        fun startOfTodayMillis(): Long =
            Calendar.getInstance().let { calendar ->
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
    }
}
