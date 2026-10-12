package com.bekaryab.qadamevents.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bekaryab.qadamevents.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AuthUiState()
    }
    fun register(
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val normalizedEmail = email.trim()

        when {
            !Patterns.EMAIL_ADDRESS
                .matcher(normalizedEmail).matches() -> {
                showError("Email форматын тексеріңіз.")
                return
            }

            password.length < 6 -> {
                showError("Құпиясөз кемінде 6 таңба болуы керек.")
                return
            }

            password != confirmPassword -> {
                showError("Құпиясөздер бірдей емес.")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.register(
                email = normalizedEmail,
                password = password
            )

            _uiState.value = result.fold(
                onSuccess = { user ->
                    AuthUiState(
                        user = user,
                        successMessage = "Аккаунт сәтті құрылды."
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

    private fun showError(message: String) {
        _uiState.value = AuthUiState(
            errorMessage = message
        )
    }
}