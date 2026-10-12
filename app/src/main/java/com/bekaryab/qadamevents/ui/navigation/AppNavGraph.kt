
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
import com.bekaryab.qadamevents.ui.profile.ProfileHeader

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
            // Minimum TabBar integration: tab state lives here until real
            // destinations exist. Other tabs show safe placeholders; Profile
            // keeps the existing account screen (email + logout) untouched.
            var tab by remember { mutableStateOf(QadamTab.Profile) }

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
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Upper profile-header section only; the existing
                            // account block below keeps email + logout working.
                            ProfileHeader()

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AuthSuccessScreen(
                                    email = currentUser?.email.orEmpty(),
                                    onLogout = {
                                        loginViewModel.logout()
                                        registerViewModel.resetState()
                                        destination = "login"
                                    }
                                )
                            }
                        }
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

@Composable
private fun AuthSuccessScreen(
    email: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Qadam Events",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Авторизация сәтті өтті!",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(8.dp))

        Text(text = email)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Шығу")
        }
    }
}
