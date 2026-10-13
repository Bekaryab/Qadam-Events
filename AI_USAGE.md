# AI Usage Record — SIS 4: Refactor Your App to MVVM

This file honestly records where an AI assistant was used while building the
SIS 4 Events slice of Qadam Events, and where the work was verified by hand.

## What the AI helped with

- **Planning the MVVM slice:** breaking the assignment into data layer,
  ViewModel/state, stateless screen + Route, previews, logging, debug entry,
  and docs, in an order that never broke the existing app.
- **Reviewing architecture:** checking each new file against the SIS 4 rules
  (private `MutableStateFlow` / public `StateFlow`, `copy()` updates, no
  ViewModel inside screens, no filtering in composables).
- **Suggesting implementation steps:** e.g. putting `delay(1500)` and
  `shouldFail` in the repository (not in screens), keeping the instant
  `events` flow untouched for Profile/Explore/Create, and using
  `CreationExtras.createSavedStateHandle()` in the factory.
- **Debugging compile errors:** unreadable-import fixes, a `PaddingValues`
  overload mistake, a malformed vector XML file, a stray brace, and a bad
  test fixture — each fixed against the actual compiler message.
- **Writing documentation:** drafting this file, the README section, and the
  rewritten click trace from the real source.

## What was inspected and tested by hand (not by the AI)

- Every source file named in the README section was read directly, including
  confirming that `EventRepositoryImpl.kt` no longer exists and the live
  class is `InMemoryEventRepository`.
- `./gradlew :app:compileDebugKotlin` and `./gradlew :app:testDebugUnitTest`
  were run; 7/7 tests pass (6 filter tests + 1 pre-existing example test).
- The emulator rotation check was performed manually by the student: after
  rotating, the Activity was destroyed/recreated while the search query,
  category, and favorites stayed visible, and the captured log excerpt showed
  MainActivity lifecycle events with no new `EventsViewModel` init and no
  `onCleared`. The AI never saw Logcat and makes no claim beyond that.
- Preview rendering was **not** verified (compile only), and no runtime
  behavior beyond the above was tested.

## Misplaced logic: filtering inside the composable — fixed by the refactor

**Real example from this project.** `ui/profile/ProfileScreen.kt` still
filters its saves/likes lists directly in composition:

```kotlin
val saved = allEvents.filter { it.id in savedSet }
val liked = allEvents.filter { it.id in likedSet }
```

That logic is untestable on plain JVM (it lives inside a `@Composable`),
re-runs on every recomposition, and can't survive process death. The SIS 4
slice does the same job in the right layer instead:
`EventsViewModel.onSearchQueryChanged()` / `onCategorySelected()` compute
`visibleEvents` with the pure top-level `filterEvents()` function in
`ui/events/list/EventsViewModel.kt`, write the inputs to `SavedStateHandle`,
and emit one immutable `EventsUiState` via `copy()`.

Why the ViewModel (or data layer) owns this instead of a composable:

1. **Testability** — `filterEvents()` has six plain-JUnit tests; composable
   code cannot be unit-tested this way.
2. **Single source of truth** — one recompute point instead of filtering
   scattered across screens that can disagree.
3. **Rotation survival** — inputs live in `SavedStateHandle`, so the same
   filtered list rebuilds after recreation; composable `remember` state does
   not survive process death.
4. **Unidirectional flow** — UI emits events up (`onSearchQueryChanged`) and
   renders state down (`visibleEvents`); the screen stays a pure function of
   state, which is exactly what the four Previews demonstrate.

## Defense notes (5–7 minutes)

- Draw the flow on paper first: Screen → Route → ViewModel → UseCase →
  Repository → data, UiState back down. Name the real files.
- Demo the four states by toggling `shouldFail` and searching gibberish.
- Explain *why* the delay lives in the repository (it simulates the server;
  screens must never sleep) and why the instant `events` flow stayed
  delay-free (Profile/Explore/Create share it).
- Be ready to admit limits: no emulator proof for previews, no journey
  tests, in-memory data only.
