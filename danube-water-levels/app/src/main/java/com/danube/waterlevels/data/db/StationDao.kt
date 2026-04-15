package com.danube.waterlevels.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {

    @Query("SELECT * FROM stations ORDER BY km ASC")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE shortname LIKE '%' || :query || '%' OR longname LIKE '%' || :query || '%' ORDER BY km ASC")
    fun searchStations(query: String): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE uuid = :uuid")
    suspend fun getStation(uuid: String): StationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<StationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: StationEntity)

    @Query("SELECT * FROM measurements WHERE stationUuid = :stationUuid ORDER BY timestamp ASC")
    fun getMeasurements(stationUuid: String): Flow<List<MeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<MeasurementEntity>)

    @Query("DELETE FROM measurements WHERE stationUuid = :stationUuid")
    suspend fun deleteMeasurements(stationUuid: String)
}
