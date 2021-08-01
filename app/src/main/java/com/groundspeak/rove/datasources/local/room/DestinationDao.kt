package com.groundspeak.rove.datasources.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DestinationDao {
    @Query("SELECT * FROM destinations")
    fun getAll(): List<DestinationEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(destinations: List<DestinationEntity>)
}
