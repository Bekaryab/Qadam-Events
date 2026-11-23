package com.bekaryab.qadamevents.ui.events.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * Thin bridge between [EventsViewModel] and the stateless [EventsScreen].
 * Owns no state itself: it observes the ViewModel with a lifecycle-aware
 * collector and forwards state plus callbacks.
 */
@Composable
fun EventsRoute(
    viewModel: EventsViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val favoriteEventIds by viewModel.favoriteEventIds.collectAsStateWithLifecycle()

    // Initial load only: a retained ViewModel in Success/Error must not refetch
    // after rotation. A fresh ViewModel always starts in Loading.
    LaunchedEffect(Unit) {
        if (viewModel.uiState.value is EventsUiState.Loading) {
            viewModel.loadEvents()
        }
    }

    EventsScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        selectedCategory = selectedCategory,
        favoriteEventIds = favoriteEventIds,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onFavoriteClick = viewModel::onFavoriteClick,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}
