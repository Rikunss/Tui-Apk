package com.tui.downloader.downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

/**
 * Foreground service yang menjalankan queue download.
 * Service ini tetap hidup selama ada task yang berjalan.
 */
class DownloadManagerService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val repo by lazy { DownloadRepository.getInstance(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        startQueueWorker()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun startQueueWorker() {
        scope.launch {
            while (isActive) {
                val task = repo.getNextPending() ?: break
                try {
                    task.start()
                } catch (_: CancellationException) {}
                catch (e: Exception) { e.printStackTrace() }
            }
            stopSelf()
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "tui_downloader_channel"
        val name = "Tui Downloader Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notif: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tui Downloader")
            .setContentText("Downloading...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()

        startForeground(1, notif)
    }
}
