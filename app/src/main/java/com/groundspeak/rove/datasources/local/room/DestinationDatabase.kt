package com.groundspeak.rove.datasources.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(DestinationEntity::class), version = 1)
abstract class DestinationDatabase : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao
}
