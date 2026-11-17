package com.bekaryab.qadamevents.ui.explore

import com.bekaryab.qadamevents.data.repository.InMemoryEventRepository
import com.bekaryab.qadamevents.domain.model.Event
import com.bekaryab.qadamevents.domain.repository.RemoteEventRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadingStateIsShownUntilResponseArrives() = runTest(dispatcher) {
        val gate = CompletableDeferred<List<Event>>()
        val remote = FakeRemoteEventRepository().apply {
            result = { gate.await() }
        }
        val vm = viewModel(remote)

        vm.loadCommunityPosts()
        runCurrent()

        assertTrue(vm.communityPosts.value is CommunityPostsUiState.Loading)
        assertEquals(1, remote.calls)

        gate.complete(listOf(post(id = "1")))
        advanceUntilIdle()

        val state = vm.communityPosts.value
        assertTrue(state is CommunityPostsUiState.Success)
        assertEquals(1, (state as CommunityPostsUiState.Success).posts.size)
    }

    @Test
    fun successfulLoadPublishesPosts() = runTest(dispatcher) {
        val remote = FakeRemoteEventRepository().apply {
            result = { listOf(post(id = "1"), post(id = "2")) }
        }
        val vm = viewModel(remote)

        vm.loadCommunityPosts()
        advanceUntilIdle()

        val state = vm.communityPosts.value as CommunityPostsUiState.Success
        assertEquals(2, state.posts.size)
        assertEquals(1, remote.calls)
    }

    @Test
    fun emptyResponsePublishesEmptySuccess() = runTest(dispatcher) {
        val remote = FakeRemoteEventRepository().apply {
            result = { emptyList() }
        }
        val vm = viewModel(remote)

        vm.loadCommunityPosts()
        advanceUntilIdle()

        val state = vm.communityPosts.value as CommunityPostsUiState.Success
        assertTrue(state.posts.isEmpty())
    }

    @Test
    fun ioFailurePublishesError() = runTest(dispatcher) {
        val remote = FakeRemoteEventRepository().apply {
            result = { throw IOException("offline") }
        }
        val vm = viewModel(remote)

        vm.loadCommunityPosts()
        advanceUntilIdle()

        val state = vm.communityPosts.value as CommunityPostsUiState.Error
        assertTrue(state.message.contains("Couldn't reach"))
    }

    @Test
    fun httpFailurePublishesServerError() = runTest(dispatcher) {
        val body = "".toResponseBody("application/json".toMediaType())
        val remote = FakeRemoteEventRepository().apply {
            result = { throw HttpException(Response.error<Any>(500, body)) }
        }
        val vm = viewModel(remote)

        vm.loadCommunityPosts()
        advanceUntilIdle()

        val state = vm.communityPosts.value as CommunityPostsUiState.Error
        assertTrue(state.message.contains("500"))
    }

    @Test
    fun retryPerformsAnotherRequest() = runTest(dispatcher) {
        var shouldFail = true
        val remote = FakeRemoteEventRepository().apply {
            result = {
                if (shouldFail) throw IOException("offline")
                listOf(post())
            }
        }
        val vm = viewModel(remote)

        vm.loadCommunityPosts()
        advanceUntilIdle()
        assertTrue(vm.communityPosts.value is CommunityPostsUiState.Error)
        assertEquals(1, remote.calls)

        shouldFail = false
        vm.retryCommunityPosts()
        advanceUntilIdle()

        assertTrue(vm.communityPosts.value is CommunityPostsUiState.Success)
        assertEquals(2, remote.calls)
    }

    @Test
    fun duplicateLoadIsIgnored() = runTest(dispatcher) {
        val remote = FakeRemoteEventRepository().apply {
            result = { listOf(post()) }
        }
        val vm = viewModel(remote)

        vm.loadCommunityPosts()
        vm.loadCommunityPosts()
        advanceUntilIdle()

        assertEquals(1, remote.calls)
    }

    @Test
    fun searchFiltersPostsLocally() = runTest(dispatcher) {
        val remote = FakeRemoteEventRepository().apply {
            result = {
                listOf(
                    post(id = "1", title = "Kotlin post"),
                    post(id = "2", title = "Design post")
                )
            }
        }
        val vm = viewModel(remote)

        vm.loadCommunityPosts()
        advanceUntilIdle()
        vm.setQuery("kotlin")

        val state = vm.communityPosts.value as CommunityPostsUiState.Success
        assertEquals(1, state.posts.size)
        assertEquals("Kotlin post", state.posts.first().title)
    }

    private fun viewModel(remote: FakeRemoteEventRepository): ExploreViewModel =
        ExploreViewModel(InMemoryEventRepository(), remote)

    private fun post(
        id: String = "1",
        title: String = "Example post title",
        description: String = "Example post content",
        category: String = "History"
    ) = Event(
        id = id,
        title = title,
        location = "Online · User #121",
        time = "305 views · 192 likes",
        dateBadge = "POST",
        isUpcoming = false,
        description = description,
        category = category
    )

    private class FakeRemoteEventRepository : RemoteEventRepository {
        var result: suspend () -> List<Event> = { emptyList() }
        var calls = 0
            private set

        override suspend fun getEvents(): List<Event> {
            calls++
            return result()
        }
    }
}
