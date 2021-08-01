package com.groundspeak.rove.datasources.local.room

import com.groundspeak.rove.datasources.local.LocalDestinationDataSource
import com.groundspeak.rove.models.Destination

class RoomDestinationDataSource(
    val destinationDatabase: DestinationDatabase
) : LocalDestinationDataSource {
    override suspend fun insert(destinations: List<Destination>) {
        destinationDatabase.destinationDao().insertAll(
            destinations.map {
                DestinationEntity(
                    id = it.id,
                    title = it.title,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    radius = it.radius,
                    message = it.message
                )
            }
        )
    }

    override suspend fun request(): List<Destination>? {
        return destinationDatabase.destinationDao().getAll()?.map {
            Destination(
                id = it.id,
                title = it.title,
                latitude = it.latitude,
                longitude = it.longitude,
                radius = it.radius,
                message = it.message
            )
        }
    }
}
