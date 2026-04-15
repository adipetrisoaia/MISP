package com.danube.waterlevels.ui.stationdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danube.waterlevels.data.db.MeasurementEntity
import com.danube.waterlevels.data.db.StationEntity
import com.danube.waterlevels.data.repository.StationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationDetailViewModel @Inject constructor(
    private val repository: StationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stationUuid: String = savedStateHandle.get<String>("station_uuid") ?: ""

    private val _station = MutableLiveData<StationEntity?>()
    val station: LiveData<StationEntity?> = _station

    private val _measurements = MutableLiveData<List<MeasurementEntity>>()
    val measurements: LiveData<List<MeasurementEntity>> = _measurements

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        if (stationUuid.isNotBlank()) {
            loadStation()
            observeMeasurements()
            refresh()
        }
    }

    private fun loadStation() {
        viewModelScope.launch {
            _station.value = repository.getStation(stationUuid)
        }
    }

    private fun observeMeasurements() {
        viewModelScope.launch {
            repository.getMeasurements(stationUuid).collectLatest {
                _measurements.value = it
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val levelResult = repository.refreshCurrentLevel(stationUuid)
            val measurementResult = repository.refreshMeasurements(stationUuid)

            if (levelResult.isFailure && measurementResult.isFailure) {
                _error.value = "Failed to load data. Showing cached values."
            }

            // Reload station after updating level
            _station.value = repository.getStation(stationUuid)
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
