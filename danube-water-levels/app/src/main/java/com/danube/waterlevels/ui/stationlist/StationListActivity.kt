package com.danube.waterlevels.ui.stationlist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.danube.waterlevels.R
import com.danube.waterlevels.databinding.ActivityStationListBinding
import com.danube.waterlevels.ui.stationdetail.StationDetailActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StationListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStationListBinding
    private val viewModel: StationListViewModel by viewModels()
    private lateinit var adapter: StationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = StationAdapter { station ->
            val intent = Intent(this, StationDetailActivity::class.java).apply {
                putExtra(StationDetailActivity.EXTRA_STATION_UUID, station.uuid)
                putExtra(StationDetailActivity.EXTRA_STATION_NAME, station.longname)
            }
            startActivity(intent)
        }
        binding.recyclerStations.layoutManager = LinearLayoutManager(this)
        binding.recyclerStations.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshStations()
        }
    }

    private fun observeViewModel() {
        viewModel.stations.observe(this) { stations ->
            adapter.submitList(stations)
            binding.textEmpty.visibility = if (stations.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage != null) {
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                    .setAction("Retry") { viewModel.refreshStations() }
                    .show()
                viewModel.clearError()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_station_list, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText.orEmpty())
                return true
            }
        })
        return true
    }
}
