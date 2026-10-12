package com.bekaryab.qadamevents.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bekaryab.qadamevents.domain.repository.AuthRepository

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        return when {
            modelClass.isAssignableFrom(
                LoginViewModel::class.java
            ) -> LoginViewModel(authRepository) as T

            modelClass.isAssignableFrom(
                RegisterViewModel::class.java
            ) -> RegisterViewModel(authRepository) as T

            else -> throw IllegalArgumentException(
                "Unknown ViewModel: ${modelClass.name}"
            )
        }
    }
}