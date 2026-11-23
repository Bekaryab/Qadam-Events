package com.bekaryab.qadamevents.ui.explore
import com.bekaryab.qadamevents.domain.repository.LocationRepository
sealed interface ExploreLocationUiState {

    // Location has not been requested yet
    object Idle : ExploreLocationUiState

    // Location is being determined
    object Loading : ExploreLocationUiState

    // City was successfully detected
    data class Success(
        val city: String
    ) : ExploreLocationUiState

    // An error occurred
    data class Error(
        val message: String
    ) : ExploreLocationUiState
}