package com.danube.waterlevels.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey
    val uuid: String,
    val number: String,
    val shortname: String,
    val longname: String,
    val km: Double,
    val agency: String,
    val longitude: Double?,
    val latitude: Double?,
    val waterName: String,
    val currentLevel: Double?,
    val currentTimestamp: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "measurements",
    primaryKeys = ["stationUuid", "timestamp"],
    foreignKeys = [
        ForeignKey(
            entity = StationEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["stationUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stationUuid")]
)
data class MeasurementEntity(
    val stationUuid: String,
    val timestamp: String,
    val value: Double
)
