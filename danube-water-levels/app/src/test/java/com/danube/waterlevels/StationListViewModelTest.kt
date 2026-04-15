package com.danube.waterlevels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.danube.waterlevels.data.db.StationEntity
import com.danube.waterlevels.data.repository.StationRepository
import com.danube.waterlevels.ui.stationlist.StationListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class StationListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: StationRepository
    private lateinit var viewModel: StationListViewModel

    private val testStations = listOf(
        StationEntity(
            uuid = "uuid-1",
            number = "10001",
            shortname = "PASSAU",
            longname = "Passau / Donau",
            km = 2225.2,
            agency = "WSA Regensburg",
            longitude = 13.47,
            latitude = 48.57,
            waterName = "DONAU",
            currentLevel = 350.0,
            currentTimestamp = "2024-01-15T10:00:00+01:00"
        ),
        StationEntity(
            uuid = "uuid-2",
            number = "10002",
            shortname = "HOFKIRCHEN",
            longname = "Hofkirchen / Donau",
            km = 2257.0,
            agency = "WSA Regensburg",
            longitude = 13.12,
            latitude = 48.67,
            waterName = "DONAU",
            currentLevel = 280.0,
            currentTimestamp = "2024-01-15T10:00:00+01:00"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        whenever(repository.getStations()).thenReturn(flowOf(testStations))
        whenever(repository.searchStations("pass")).thenReturn(
            flowOf(listOf(testStations[0]))
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads stations`() = runTest(testDispatcher) {
        whenever(repository.refreshStations()).thenReturn(Result.success(Unit))

        viewModel = StationListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val stations = viewModel.stations.value
        assertEquals(2, stations?.size)
        assertEquals("PASSAU", stations?.get(0)?.shortname)
    }

    @Test
    fun `refresh failure sets error message`() = runTest(testDispatcher) {
        whenever(repository.refreshStations())
            .thenReturn(Result.failure(RuntimeException("Network error")))

        viewModel = StationListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Network error", viewModel.error.value)
        assertFalse(viewModel.isLoading.value ?: true)
    }

    @Test
    fun `search filters stations`() = runTest(testDispatcher) {
        whenever(repository.refreshStations()).thenReturn(Result.success(Unit))

        viewModel = StationListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.search("pass")
        testDispatcher.scheduler.advanceUntilIdle()

        val stations = viewModel.stations.value
        assertEquals(1, stations?.size)
        assertEquals("PASSAU", stations?.get(0)?.shortname)
    }
}
