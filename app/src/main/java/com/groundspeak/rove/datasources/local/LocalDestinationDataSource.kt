package com.groundspeak.rove.datasources.local

import com.groundspeak.rove.datasources.DestinationDataSource
import com.groundspeak.rove.models.Destination

interface LocalDestinationDataSource : DestinationDataSource {
    suspend fun insert(destinations: List<Destination>)
}
