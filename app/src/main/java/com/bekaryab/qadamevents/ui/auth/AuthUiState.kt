package com.bekaryab.qadamevents.ui.auth

import com.bekaryab.qadamevents.domain.model.AuthUser

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: AuthUser? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)