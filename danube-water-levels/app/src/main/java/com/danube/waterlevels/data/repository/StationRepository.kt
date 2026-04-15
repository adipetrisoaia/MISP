package com.danube.waterlevels.data.repository

import com.danube.waterlevels.data.api.PegelOnlineApi
import com.danube.waterlevels.data.db.MeasurementEntity
import com.danube.waterlevels.data.db.StationDao
import com.danube.waterlevels.data.db.StationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationRepository @Inject constructor(
    private val api: PegelOnlineApi,
    private val stationDao: StationDao
) {

    fun getStations(): Flow<List<StationEntity>> = stationDao.getAllStations()

    fun searchStations(query: String): Flow<List<StationEntity>> = stationDao.searchStations(query)

    fun getMeasurements(stationUuid: String): Flow<List<MeasurementEntity>> =
        stationDao.getMeasurements(stationUuid)

    suspend fun getStation(uuid: String): StationEntity? = stationDao.getStation(uuid)

    /**
     * Fetches stations from the API and caches them locally.
     * Returns a [Result] to communicate success or failure to the UI.
     */
    suspend fun refreshStations(): Result<Unit> {
        return try {
            val remoteStations = api.getStations()
            val entities = remoteStations.map { station ->
                StationEntity(
                    uuid = station.uuid,
                    number = station.number,
                    shortname = station.shortname,
                    longname = station.longname,
                    km = station.km,
                    agency = station.agency,
                    longitude = station.longitude,
                    latitude = station.latitude,
                    waterName = station.water.shortname,
                    currentLevel = null,
                    currentTimestamp = null
                )
            }
            stationDao.insertStations(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches the current water level for a station and updates the cache.
     */
    suspend fun refreshCurrentLevel(stationUuid: String): Result<Double> {
        return try {
            val measurement = api.getCurrentMeasurement(stationUuid)
            val station = stationDao.getStation(stationUuid)
            if (station != null) {
                stationDao.insertStation(
                    station.copy(
                        currentLevel = measurement.value,
                        currentTimestamp = measurement.timestamp,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
            Result.success(measurement.value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches 7-day measurement history for a station.
     */
    suspend fun refreshMeasurements(stationUuid: String): Result<Unit> {
        return try {
            val measurements = api.getMeasurements(stationUuid)
            val entities = measurements.map { m ->
                MeasurementEntity(
                    stationUuid = stationUuid,
                    timestamp = m.timestamp,
                    value = m.value
                )
            }
            stationDao.deleteMeasurements(stationUuid)
            stationDao.insertMeasurements(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refreshes all station current levels (used by background worker).
     */
    suspend fun refreshAllCurrentLevels(): Result<Unit> {
        return try {
            val stations = api.getStations()
            for (station in stations) {
                try {
                    val measurement = api.getCurrentMeasurement(station.uuid)
                    stationDao.insertStation(
                        StationEntity(
                            uuid = station.uuid,
                            number = station.number,
                            shortname = station.shortname,
                            longname = station.longname,
                            km = station.km,
                            agency = station.agency,
                            longitude = station.longitude,
                            latitude = station.latitude,
                            waterName = station.water.shortname,
                            currentLevel = measurement.value,
                            currentTimestamp = measurement.timestamp
                        )
                    )
                } catch (_: Exception) {
                    // Skip individual station failures during bulk refresh
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
