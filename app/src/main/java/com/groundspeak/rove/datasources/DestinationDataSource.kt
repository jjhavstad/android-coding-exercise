package com.groundspeak.rove.datasources

import com.groundspeak.rove.models.Destination
import kotlin.jvm.Throws

interface DestinationDataSource {
    @Throws(Exception::class)
    suspend fun request(): List<Destination>?
}
