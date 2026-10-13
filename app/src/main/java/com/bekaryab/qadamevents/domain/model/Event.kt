package com.bekaryab.qadamevents.domain.model

/**
 * Single event model for the app. Display strings ([time], [dateBadge]) are
 * preformatted so cards render without parsing; the structured fields below
 * carry the form data. New fields default so existing call sites (Reels demo,
 * old samples) keep compiling unchanged.
 */
data class Event(
    val id: String,
    val title: String,
    val location: String,
    val time: String,
    val dateBadge: String,
    val isUpcoming: Boolean = true,
    val description: String = "",
    val category: String = "Other",
    val coverImageUri: String? = null,
    val ownerUid: String? = null,
    val startMillis: Long? = null,
    val endMillis: Long? = null
)
