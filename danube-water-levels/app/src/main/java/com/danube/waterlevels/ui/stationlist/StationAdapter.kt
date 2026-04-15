package com.danube.waterlevels.ui.stationlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danube.waterlevels.data.db.StationEntity
import com.danube.waterlevels.databinding.ItemStationBinding
import java.util.Locale

class StationAdapter(
    private val onStationClick: (StationEntity) -> Unit
) : ListAdapter<StationEntity, StationAdapter.StationViewHolder>(StationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val binding = ItemStationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StationViewHolder(binding, onStationClick)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StationViewHolder(
        private val binding: ItemStationBinding,
        private val onStationClick: (StationEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(station: StationEntity) {
            binding.textStationName.text = station.longname
            binding.textStationKm.text = "km ${String.format(Locale.US, "%.1f", station.km)}"
            binding.textAgency.text = station.agency

            if (station.currentLevel != null) {
                binding.textCurrentLevel.text = String.format(
                    Locale.US, "%.0f cm", station.currentLevel
                )
            } else {
                binding.textCurrentLevel.text = "—"
            }

            binding.root.setOnClickListener { onStationClick(station) }
        }
    }

    class StationDiffCallback : DiffUtil.ItemCallback<StationEntity>() {
        override fun areItemsTheSame(oldItem: StationEntity, newItem: StationEntity): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: StationEntity, newItem: StationEntity): Boolean {
            return oldItem == newItem
        }
    }
}
