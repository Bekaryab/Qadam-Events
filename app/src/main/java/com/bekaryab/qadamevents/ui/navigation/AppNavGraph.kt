
package com.bekaryab.qadamevents.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.bekaryab.qadamevents.domain.repository.AuthRepository
import com.bekaryab.qadamevents.ui.auth.LoginScreen
import com.bekaryab.qadamevents.ui.auth.LoginViewModel
import com.bekaryab.qadamevents.ui.auth.RegisterScreen
import com.bekaryab.qadamevents.ui.auth.RegisterViewModel
import com.bekaryab.qadamevents.ui.events.create.CreateEventScreen
import com.bekaryab.qadamevents.ui.events.create.CreateEventViewModel
import com.bekaryab.qadamevents.ui.events.manage.EventManageViewModel
import com.bekaryab.qadamevents.ui.explore.ExploreScreen
import com.bekaryab.qadamevents.ui.explore.ExploreViewModel
import com.bekaryab.qadamevents.ui.profile.ProfileScreen
import com.bekaryab.qadamevents.ui.profile.ProfileSection
import com.bekaryab.qadamevents.ui.reels.ReelsScreen
import com.bekaryab.qadamevents.ui.reels.ReelsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavGraph(
    loginViewModel: LoginViewModel = koinViewModel(),
    registerViewModel: RegisterViewModel = koinViewModel()
) {
    val loginState by loginViewModel.uiState.collectAsState()
    val registerState by registerViewModel.uiState.collectAsState()

    // Firebase session state.
    val authRepository: AuthRepository = koinInject()
    val sessionUser by authRepository.authState.collectAsState()

    // null = session is still resolving.
    var destination by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(sessionUser) {
        if (sessionUser != null) {
            if (destination != "home") {
                destination = "home"
            }
        } else if (destination == null || destination == "home") {
            destination = "login"
        }
    }

    val currentUser = sessionUser
        ?: loginState.user
        ?: registerState.user

    when (destination) {

        "register" -> {
            RegisterScreen(
                state = registerState,
                onRegister = registerViewModel::register,
                onOpenLogin = {
                    destination = "login"
                }
            )
        }

        "home" -> {

            // Bottom navigation state.
            var tab by remember {
                mutableStateOf(QadamTab.Profile)
            }

            val reelsViewModel: ReelsViewModel = koinViewModel()

            // Shared events repository.
            val eventRepository: com.bekaryab.qadamevents.domain.repository.EventRepository = koinInject()
            val allEvents by eventRepository.events.collectAsState()

            // Create event ViewModel.
            val createViewModel: CreateEventViewModel = koinViewModel()

            // Location-enabled Explore ViewModel.
            val exploreViewModel: ExploreViewModel = koinViewModel()

            // Event management ViewModel.
            val manageViewModel: EventManageViewModel = koinViewModel()

            val manageState by manageViewModel.uiState.collectAsState()

            var profileSectionName by rememberSaveable {
                mutableStateOf(ProfileSection.MY_EVENTS.name)
            }

            val currentUid = currentUser?.uid

            // Active events belonging to the current user.
            val myEvents = allEvents.filter {
                it.ownerUid == currentUid && !it.isArchived
            }

            if (tab == QadamTab.Reels) {

                // Reels overlay.
                Box(modifier = Modifier.fillMaxSize()) {

                    val navBottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()

                    ReelsScreen(
                        viewModel = reelsViewModel,
                        bottomOverlay = navBottom + 72.dp,
                        modifier = Modifier.fillMaxSize()
                    )

                    QadamBottomBar(
                        selectedTab = tab,
                        onTabSelected = { tab = it },
                        translucent = true,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp)
                    )
                }

            } else {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFDDF3F5))
                ) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {

                        if (tab == QadamTab.Profile) {

                            ProfileScreen(
                                email = currentUser?.email.orEmpty(),

                                onLogout = {
                                    loginViewModel.logout()
                                    registerViewModel.resetState()
                                    destination = "login"
                                },

                                section = ProfileSection.valueOf(profileSectionName),

                                onSectionChange = {
                                    profileSectionName = it.name
                                },

                                myEvents = myEvents,
                                allEvents = allEvents,

                                onCreateEvent = {
                                    createViewModel.resetForm()
                                    tab = QadamTab.Create
                                },

                                onEditEvent = { event ->
                                    createViewModel.loadForEdit(event)
                                    tab = QadamTab.Create
                                },

                                onArchiveEvent = { event ->
                                    manageViewModel.archiveEvent(
                                        event.id,
                                        currentUid
                                    )
                                },

                                onRestoreEvent = { event ->
                                    manageViewModel.restoreEvent(
                                        event.id,
                                        currentUid
                                    )
                                },

                                onDeleteEvent = { event ->
                                    manageViewModel.deleteEvent(
                                        event.id,
                                        currentUid
                                    )
                                },

                                manageNotice = manageState.notice,
                                manageError = manageState.errorMessage,

                                onConsumeManageFeedback =
                                    manageViewModel::consumeFeedback
                            )

                        } else if (tab == QadamTab.Explore) {

                            ExploreScreen(
                                viewModel = exploreViewModel
                            )

                        } else if (tab == QadamTab.Create) {

                            CreateEventScreen(
                                viewModel = createViewModel,
                                onSaved = {
                                    createViewModel.resetForm()
                                    tab = QadamTab.Profile
                                    profileSectionName =
                                        ProfileSection.MY_EVENTS.name
                                }
                            )

                        } else {

                            HomePlaceholderTab(label = tab.label)

                        }
                    }

                    // Bottom navigation.
                    QadamBottomBar(
                        selectedTab = tab,
                        onTabSelected = { tab = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }

        "login" -> {

            LoginScreen(
                state = loginState,
                onLogin = loginViewModel::login,
                onOpenRegister = {
                    destination = "register"
                }
            )
        }

        // Session is still resolving.
        else -> {
            AuthResolvingScreen()
        }
    }
}

@Composable
private fun AuthResolvingScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBFE0E1)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HomePlaceholderTab(label: String) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Coming soon",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
