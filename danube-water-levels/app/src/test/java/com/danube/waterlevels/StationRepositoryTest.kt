package com.danube.waterlevels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.danube.waterlevels.data.api.MeasurementResponse
import com.danube.waterlevels.data.api.PegelOnlineApi
import com.danube.waterlevels.data.api.StationResponse
import com.danube.waterlevels.data.api.WaterResponse
import com.danube.waterlevels.data.db.MeasurementEntity
import com.danube.waterlevels.data.db.StationDao
import com.danube.waterlevels.data.db.StationEntity
import com.danube.waterlevels.data.repository.StationRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class StationRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var api: PegelOnlineApi
    private lateinit var dao: StationDao
    private lateinit var repository: StationRepository

    private val testStation = StationResponse(
        uuid = "test-uuid-1",
        number = "10001",
        shortname = "PASSAU",
        longname = "Passau / Donau",
        km = 2225.2,
        agency = "WSA Regensburg",
        longitude = 13.47,
        latitude = 48.57,
        water = WaterResponse("DONAU", "Donau")
    )

    private val testStationEntity = StationEntity(
        uuid = "test-uuid-1",
        number = "10001",
        shortname = "PASSAU",
        longname = "Passau / Donau",
        km = 2225.2,
        agency = "WSA Regensburg",
        longitude = 13.47,
        latitude = 48.57,
        waterName = "DONAU",
        currentLevel = null,
        currentTimestamp = null
    )

    @Before
    fun setup() {
        api = mock()
        dao = mock()
        repository = StationRepository(api, dao)
    }

    @Test
    fun `refreshStations fetches from API and inserts into database`() = runTest {
        whenever(api.getStations(any())).thenReturn(listOf(testStation))

        val result = repository.refreshStations()

        assertTrue(result.isSuccess)
        verify(dao).insertStations(any())
    }

    @Test
    fun `refreshStations returns failure on API error`() = runTest {
        whenever(api.getStations(any())).thenThrow(RuntimeException("Network error"))

        val result = repository.refreshStations()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `refreshCurrentLevel updates station with current measurement`() = runTest {
        val measurement = MeasurementResponse(
            timestamp = "2024-01-15T10:00:00+01:00",
            value = 350.0,
            stateMnwMhw = "normal",
            stateNswHsw = null
        )
        whenever(api.getCurrentMeasurement("test-uuid-1")).thenReturn(measurement)
        whenever(dao.getStation("test-uuid-1")).thenReturn(testStationEntity)

        val result = repository.refreshCurrentLevel("test-uuid-1")

        assertTrue(result.isSuccess)
        assertEquals(350.0, result.getOrNull()!!, 0.01)
        verify(dao).insertStation(any())
    }

    @Test
    fun `refreshCurrentLevel returns failure on API error`() = runTest {
        whenever(api.getCurrentMeasurement("test-uuid-1"))
            .thenThrow(RuntimeException("Connection refused"))

        val result = repository.refreshCurrentLevel("test-uuid-1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `refreshMeasurements fetches and stores measurement history`() = runTest {
        val measurements = listOf(
            MeasurementResponse("2024-01-14T10:00:00+01:00", 340.0, null, null),
            MeasurementResponse("2024-01-14T10:15:00+01:00", 342.0, null, null),
            MeasurementResponse("2024-01-14T10:30:00+01:00", 345.0, null, null)
        )
        whenever(api.getMeasurements("test-uuid-1", "P7D")).thenReturn(measurements)

        val result = repository.refreshMeasurements("test-uuid-1")

        assertTrue(result.isSuccess)
        verify(dao).deleteMeasurements("test-uuid-1")
        verify(dao).insertMeasurements(any())
    }

    @Test
    fun `getStations returns flow from DAO`() = runTest {
        val entities = listOf(testStationEntity)
        whenever(dao.getAllStations()).thenReturn(flowOf(entities))

        val flow = repository.getStations()

        flow.collect { stations ->
            assertEquals(1, stations.size)
            assertEquals("PASSAU", stations[0].shortname)
        }
    }

    @Test
    fun `searchStations delegates to DAO`() = runTest {
        whenever(dao.searchStations("pass")).thenReturn(flowOf(listOf(testStationEntity)))

        val flow = repository.searchStations("pass")

        flow.collect { stations ->
            assertEquals(1, stations.size)
            assertEquals("PASSAU", stations[0].shortname)
        }
    }

    @Test
    fun `getMeasurements returns flow from DAO`() = runTest {
        val measurements = listOf(
            MeasurementEntity("test-uuid-1", "2024-01-14T10:00:00+01:00", 340.0),
            MeasurementEntity("test-uuid-1", "2024-01-14T10:15:00+01:00", 342.0)
        )
        whenever(dao.getMeasurements("test-uuid-1")).thenReturn(flowOf(measurements))

        val flow = repository.getMeasurements("test-uuid-1")

        flow.collect { result ->
            assertEquals(2, result.size)
            assertEquals(340.0, result[0].value, 0.01)
        }
    }
}
