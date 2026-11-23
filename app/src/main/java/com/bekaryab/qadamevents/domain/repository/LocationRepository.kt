package com.bekaryab.qadamevents.domain.repository

interface LocationRepository {

    suspend fun getCurrentCity(): Result<String>
}