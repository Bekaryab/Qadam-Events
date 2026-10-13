
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
import com.bekaryab.qadamevents.di.AppModule
import com.bekaryab.qadamevents.ui.auth.LoginScreen
import com.bekaryab.qadamevents.ui.auth.LoginViewModel
import com.bekaryab.qadamevents.ui.auth.RegisterScreen
import com.bekaryab.qadamevents.ui.auth.RegisterViewModel
import com.bekaryab.qadamevents.ui.events.create.CreateEventScreen
import com.bekaryab.qadamevents.ui.events.create.CreateEventViewModel
import com.bekaryab.qadamevents.ui.explore.ExploreScreen
import com.bekaryab.qadamevents.ui.explore.ExploreViewModel
import com.bekaryab.qadamevents.ui.profile.ProfileScreen
import com.bekaryab.qadamevents.ui.profile.ProfileSection
import com.bekaryab.qadamevents.ui.reels.ReelsScreen
import com.bekaryab.qadamevents.ui.reels.ReelsViewModel

@Composable
fun AppNavGraph(
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel
) {
    val loginState by loginViewModel.uiState.collectAsState()
    val registerState by registerViewModel.uiState.collectAsState()

    // Single source of truth: the Firebase session flow (persisted across
    // restarts by Firebase itself). Replaces the one-shot getCurrentUser()
    // check, so an already-authenticated user never flashes the Login page.
    val sessionUser by AppModule.authRepository.authState.collectAsState()

    // null = session still resolving -> loading splash, never the wrong screen.
    var destination by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionUser) {
        if (sessionUser != null) {
            // Authenticated: covers cold start, login success, and register
            // success (Firebase signs the user in). Guarded so repeated
            // emissions don't re-trigger navigation.
            if (destination != "home") destination = "home"
        } else if (destination == null || destination == "home") {
            // Unauthenticated at startup -> login; explicit sign-out or session
            // invalidation while on home -> login. Login/register forms are
            // never yanked away; failures simply stay put with their error.
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
                onOpenLogin = { destination = "login" }
            )
        }

        "home" -> {
            // Bottom-bar tabs live here until real destinations exist. Profile
            // renders the full Profile page (header, section tabs, content);
            // logout stays wired through Settings so the flow is preserved.
            // The Reels mockup keeps its ViewModel here so tab switches don't
            // reset its demo state (reset only when leaving home).
            var tab by remember { mutableStateOf(QadamTab.Profile) }
            val reelsViewModel = remember { ReelsViewModel() }

            // Shared event state (repository singleton): survives navigation
            // and configuration changes. Profile section is hoisted here so a
            // successful save can select MY EVENTS without duplicate state.
            // NOTE: the SIS 4 EventsViewModel now takes use cases + a
            // SavedStateHandle and is not connected to any screen yet, so the
            // profile list below reads the same repository flow directly
            // (identical instance, identical behavior as before).
            val allEvents by AppModule.eventRepository.events.collectAsState()
            val createViewModel = remember {
                CreateEventViewModel(
                    AppModule.eventRepository,
                    AppModule.authRepository
                )
            }
            val exploreViewModel = remember {
                ExploreViewModel(AppModule.eventRepository)
            }
            var profileSectionName by rememberSaveable {
                mutableStateOf(ProfileSection.MY_EVENTS.name)
            }
            val currentUid = currentUser?.uid
            val myEvents = allEvents.filter { it.ownerUid == currentUid }

            if (tab == QadamTab.Reels) {
                // Reels overlay: media fills the whole region INCLUDING behind
                // the translucent bar, so no cyan gap exists. The bar keeps its
                // single navigationBarsPadding (icons stay above gestures);
                // ReelsScreen lifts its bottom content above the bar block.
                // Bar block = 52dp bar + 12dp margin + 8dp breathing room.
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
                            onSectionChange = { profileSectionName = it.name },
                            myEvents = myEvents,
                            allEvents = allEvents,
                            onCreateEvent = { tab = QadamTab.Create }
                        )
                    } else if (tab == QadamTab.Explore) {
                        ExploreScreen(viewModel = exploreViewModel)
                    } else if (tab == QadamTab.Create) {
                        CreateEventScreen(
                            viewModel = createViewModel,
                            onSaved = {
                                tab = QadamTab.Profile
                                profileSectionName = ProfileSection.MY_EVENTS.name
                            }
                        )
                    } else {
                            HomePlaceholderTab(label = tab.label)
                        }
                    }

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
                onOpenRegister = { destination = "register" }
            )
        }

        // Session still resolving: loading state instead of the wrong screen.
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
    // Temporary stand-in until the full Home page is built.
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
