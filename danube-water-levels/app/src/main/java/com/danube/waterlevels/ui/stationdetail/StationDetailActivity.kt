package com.danube.waterlevels.ui.stationdetail

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.danube.waterlevels.R
import com.danube.waterlevels.data.db.MeasurementEntity
import com.danube.waterlevels.databinding.ActivityStationDetailBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class StationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStationDetailBinding
    private val viewModel: StationDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val stationName = intent.getStringExtra(EXTRA_STATION_NAME) ?: "Station Detail"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = stationName
            setDisplayHomeAsUpEnabled(true)
        }

        setupChart()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupChart() {
        binding.chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            legend.isEnabled = true

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
            }

            axisRight.isEnabled = false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewModel.station.observe(this) { station ->
            if (station != null) {
                binding.textCurrentLevel.text = if (station.currentLevel != null) {
                    String.format(Locale.US, "%.0f cm", station.currentLevel)
                } else {
                    "—"
                }
                binding.textStationInfo.text = getString(
                    R.string.station_info_format,
                    station.longname,
                    station.km,
                    station.agency
                )
                binding.textLastUpdated.text = if (station.currentTimestamp != null) {
                    getString(R.string.last_updated_format, formatTimestamp(station.currentTimestamp))
                } else {
                    ""
                }
            }
        }

        viewModel.measurements.observe(this) { measurements ->
            if (measurements.isNotEmpty()) {
                updateChart(measurements)
                binding.chart.visibility = View.VISIBLE
                binding.textNoData.visibility = View.GONE
            } else {
                binding.chart.visibility = View.GONE
                binding.textNoData.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage != null) {
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                    .setAction("Retry") { viewModel.refresh() }
                    .show()
                viewModel.clearError()
            }
        }
    }

    private fun updateChart(measurements: List<MeasurementEntity>) {
        val entries = measurements.mapIndexed { index, m ->
            Entry(index.toFloat(), m.value.toFloat())
        }

        val dataSet = LineDataSet(entries, "Water Level (cm)").apply {
            color = Color.parseColor("#1565C0")
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = Color.parseColor("#42A5F5")
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val dateLabels = measurements.map { formatShortTimestamp(it.timestamp) }

        binding.chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in dateLabels.indices) dateLabels[index] else ""
            }
        }

        binding.chart.data = LineData(dataSet)
        binding.chart.invalidate()
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            date?.let { outputFormat.format(it) } ?: timestamp
        } catch (_: Exception) {
            timestamp
        }
    }

    private fun formatShortTimestamp(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_STATION_UUID = "station_uuid"
        const val EXTRA_STATION_NAME = "station_name"
    }
}
