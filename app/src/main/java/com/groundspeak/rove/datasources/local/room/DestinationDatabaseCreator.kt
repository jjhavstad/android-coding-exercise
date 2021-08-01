package com.groundspeak.rove.datasources.local.room

import android.content.Context
import androidx.room.Room

object DestinationDatabaseCreator {
    fun create(context: Context): DestinationDatabase {
        return Room.databaseBuilder(
            context,
            DestinationDatabase::class.java,
            "destination"
        ).build()
    }
}
