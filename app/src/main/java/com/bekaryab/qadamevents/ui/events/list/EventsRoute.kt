package com.bekaryab.qadamevents.ui.events.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Thin bridge between [EventsViewModel] and the stateless [EventsScreen].
 * Owns no state itself: it observes the ViewModel with a lifecycle-aware
 * collector and forwards state plus callbacks. Not connected to navigation
 * yet; the screen stays fully Previewable without a ViewModel.
 */
@Composable
fun EventsRoute(
    viewModel: EventsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initial load only: after rotation the store-retained ViewModel already
    // holds data, so this guard prevents a redundant refetch.
    LaunchedEffect(Unit) {
        if (viewModel.uiState.value.allEvents.isEmpty()) {
            viewModel.loadEvents()
        }
    }

    EventsScreen(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onFavoriteClick = viewModel::onFavoriteClick,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}
