package com.bekaryab.qadamevents.ui.auth

import com.google.firebase.auth.FirebaseAuthException

internal fun Throwable.toAuthErrorMessage(): String {
    val code = (this as? FirebaseAuthException)?.errorCode

    return when (code) {
        "ERROR_INVALID_EMAIL" ->
            "Email форматы дұрыс емес."

        "ERROR_EMAIL_ALREADY_IN_USE" ->
            "Бұл email бұрын тіркелген."

        "ERROR_WEAK_PASSWORD" ->
            "Құпиясөз тым әлсіз."

        "ERROR_USER_NOT_FOUND",
        "ERROR_WRONG_PASSWORD",
        "ERROR_INVALID_CREDENTIAL",
        "INVALID_LOGIN_CREDENTIALS" ->
            "Email немесе құпиясөз қате."

        "ERROR_TOO_MANY_REQUESTS" ->
            "Тым көп әрекет жасалды. Кейінірек қайталаңыз."

        else ->
            "Қате орын алды. Интернетті тексеріп, қайта көріңіз."
    }
}