
package com.bekaryab.qadamevents.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekaryab.qadamevents.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val normalizedEmail = email.trim()

        when {
            !Patterns.EMAIL_ADDRESS
                .matcher(normalizedEmail).matches() -> {
                showError("Email форматын тексеріңіз.")
                return
            }

            password.isBlank() -> {
                showError("Құпиясөзді енгізіңіз.")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.login(
                email = normalizedEmail,
                password = password
            )

            _uiState.value = result.fold(
                onSuccess = { user ->
                    AuthUiState(
                        user = user,
                        successMessage = "Жүйеге сәтті кірдіңіз."
                    )
                },
                onFailure = { error ->
                    AuthUiState(
                        errorMessage = error.toAuthErrorMessage()
                    )
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    private fun showError(message: String) {
        _uiState.value = AuthUiState(
            errorMessage = message
        )
    }
}
