package com.bekaryab.qadamevents.ui.explore

import com.bekaryab.qadamevents.domain.model.Event

/** State of the remote community-posts section on Explore. */
sealed interface CommunityPostsUiState {

    /** Initial and retry state. */
    data object Loading : CommunityPostsUiState

    /** Posts ready to render (possibly empty). */
    data class Success(val posts: List<Event>) : CommunityPostsUiState

    /** Request failed; [message] is user-readable. */
    data class Error(val message: String) : CommunityPostsUiState
}
