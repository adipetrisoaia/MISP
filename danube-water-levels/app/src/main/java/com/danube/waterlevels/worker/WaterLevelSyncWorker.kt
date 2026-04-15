package com.danube.waterlevels.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.danube.waterlevels.DanubeApp
import com.danube.waterlevels.R
import com.danube.waterlevels.data.repository.StationRepository
import com.danube.waterlevels.ui.stationlist.StationListActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class WaterLevelSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: StationRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val result = repository.refreshAllCurrentLevels()
        if (result.isFailure) {
            return Result.retry()
        }

        checkForHighWaterLevels()
        return Result.success()
    }

    private suspend fun checkForHighWaterLevels() {
        val stations = repository.getStations().first()
        val highLevelStations = stations.filter { station ->
            station.currentLevel != null && station.currentLevel > WARNING_THRESHOLD_CM
        }

        if (highLevelStations.isNotEmpty()) {
            sendNotification(highLevelStations.size, highLevelStations.first().longname)
        }
    }

    private fun sendNotification(count: Int, exampleStation: String) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(applicationContext, StationListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (count == 1) {
            "High Water Level Alert"
        } else {
            "$count Stations with High Water Levels"
        }

        val text = if (count == 1) {
            "$exampleStation has water level above ${WARNING_THRESHOLD_CM}cm"
        } else {
            "$exampleStation and ${count - 1} other station(s) have high water levels"
        }

        val notification = NotificationCompat.Builder(applicationContext, DanubeApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "water_level_sync"
        const val WARNING_THRESHOLD_CM = 600.0
        private const val NOTIFICATION_ID = 1001
    }
}
