package com.danube.waterlevels.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StationEntity::class, MeasurementEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao

    companion object {
        const val DATABASE_NAME = "danube_water_levels_db"
    }
}
