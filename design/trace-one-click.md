# Trace One Click — Qadam Events (verified against source)

One search keystroke on the SIS 4 Events screen, end to end. Every name below
is the real function, file, or property in the repository.

1. `ui/events/list/EventsScreen.kt` — the user types in the search
   `OutlinedTextField`. Its `onValueChange` invokes the screen's
   `onSearchQueryChanged` callback parameter. The screen computes nothing.
2. `ui/events/list/EventsRoute.kt` — wired `onSearchQueryChanged =
   viewModel::onSearchQueryChanged`, so the call lands in
   `EventsViewModel.onSearchQueryChanged(query)` (`ui/events/list/EventsViewModel.kt`).
3. `EventsViewModel.onSearchQueryChanged` — writes the query to
   `SavedStateHandle` (`events_search_query` key), then
   `_uiState.update { it.copy(searchQuery = query, visibleEvents =
   filterEvents(it.allEvents, query, it.selectedCategory)) }`.
4. `filterEvents()` (same file, pure function, no Android APIs) — keeps an
   event when the query matches (case-insensitively) its title, description,
   category, or location, then applies the selected-category equality check.
5. `EventsRoute.kt` — `collectAsStateWithLifecycle()` emits the new
   `EventsUiState`; `EventsScreen` re-renders `uiState.visibleEvents` in its
   `LazyColumn` (or the Empty state when the list is empty).

Initial load and retry follow the same shape through
`loadEvents()`/`onRetry()` → `GetEventsUseCase()` →
`InMemoryEventRepository.getEvents()` (1.5 s simulated delay, `Result`
returned), with failures mapped to `errorMessage` for the Error state.
