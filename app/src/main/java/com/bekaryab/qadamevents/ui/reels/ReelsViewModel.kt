package com.bekaryab.qadamevents.ui.reels

import androidx.lifecycle.ViewModel
import com.bekaryab.qadamevents.domain.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReelsUiState(
    val feed: ReelFeed = ReelFeed.FOR_YOU,
    val liked: Boolean = false,
    val saved: Boolean = false,
    val followed: Boolean = false,
    val commentsOpen: Boolean = false,
    val moreOpen: Boolean = false
)

/**
 * Demo-only state holder for the Reels mockup, following the auth ViewModel
 * conventions (StateFlow + update). Counts come from [SampleReels]; toggles
 * start cleared and reset when the feed changes. No backend calls.
 */
class ReelsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReelsUiState())
    val uiState: StateFlow<ReelsUiState> = _uiState.asStateFlow()

    fun demoFor(feed: ReelFeed): ReelDemo = SampleReels.byFeed(feed)

    fun eventFor(feed: ReelFeed): Event = demoFor(feed).event

    fun selectFeed(feed: ReelFeed) {
        _uiState.update {
            it.copy(
                feed = feed,
                liked = false,
                saved = false,
                followed = false,
                commentsOpen = false,
                moreOpen = false
            )
        }
    }

    fun toggleLike() {
        _uiState.update { it.copy(liked = !it.liked) }
    }

    fun toggleSave() {
        _uiState.update { it.copy(saved = !it.saved) }
    }

    fun toggleFollow() {
        _uiState.update { it.copy(followed = !it.followed) }
    }

    fun setCommentsOpen(open: Boolean) {
        _uiState.update { it.copy(commentsOpen = open) }
    }

    fun setMoreOpen(open: Boolean) {
        _uiState.update { it.copy(moreOpen = open) }
    }

    companion object {
        /** 1200 -> "1.2K", 84 -> "84". Keeps toggle math consistent. */
        fun formatCount(count: Int): String {
            if (count < 1000) return count.toString()
            val value = count / 1000.0
            val rounded = kotlin.math.round(value * 10) / 10.0
            return if (rounded == kotlin.math.floor(rounded)) {
                "${rounded.toInt()}K"
            } else {
                "${rounded}K"
            }
        }
    }
}
