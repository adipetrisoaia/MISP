package com.danube.waterlevels.ui.stationlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danube.waterlevels.data.db.StationEntity
import com.danube.waterlevels.data.repository.StationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationListViewModel @Inject constructor(
    private val repository: StationRepository
) : ViewModel() {

    private val _stations = MutableLiveData<List<StationEntity>>()
    val stations: LiveData<List<StationEntity>> = _stations

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var searchJob: Job? = null
    private var currentQuery: String = ""

    init {
        observeStations()
        refreshStations()
    }

    private fun observeStations() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val flow = if (currentQuery.isBlank()) {
                repository.getStations()
            } else {
                repository.searchStations(currentQuery)
            }
            flow.collectLatest { stations ->
                _stations.value = stations
            }
        }
    }

    fun refreshStations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.refreshStations()
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load stations"
            }
            _isLoading.value = false
        }
    }

    fun search(query: String) {
        currentQuery = query
        observeStations()
    }

    fun clearError() {
        _error.value = null
    }
}
