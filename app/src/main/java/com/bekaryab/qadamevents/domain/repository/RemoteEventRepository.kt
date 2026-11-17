package com.bekaryab.qadamevents.domain.repository

import com.bekaryab.qadamevents.domain.model.Event

/**
 * Read-only contract for fetching events from a remote source.
 *
 * Implementations live in the data layer and may throw network exceptions;
 * callers are responsible for handling them.
 */
interface RemoteEventRepository {

    suspend fun getEvents(): List<Event>
}
