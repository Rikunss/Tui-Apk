package com.tui.downloader.downloader

import android.content.Context
import com.tui.downloader.telegram.TelegramUploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Menyimpan daftar task dan menyediakan API untuk mengontrolnya.
 * Ini versi in-memory (sederhana). Kalau mau persist, tinggal integrasi Room DB.
 */
class DownloadRepository private constructor(private val context: Context) {

    val tasks = mutableListOf<DownloadTask>()

    // callback untuk UI update
    @Volatile
    var onChange: (() -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.IO)
    private val telegramUploader = TelegramUploader(context)

    fun createDownload(url: String, destDir: String): DownloadTask {
        val task = DownloadTask(url, destDir) { onChange?.invoke() }
        tasks.add(task)
        onChange?.invoke()
        return task
    }

    fun getNextPending(): DownloadTask? {
        return tasks.firstOrNull { !it.isDone && !it.isPaused && !it.isError }
    }

    fun pause(task: DownloadTask) {
        task.pause()
        onChange?.invoke()
    }

    fun resume(task: DownloadTask) {
        task.isPaused = false
        onChange?.invoke()
    }

    fun uploadToTelegram(task: DownloadTask) {
        val fileName = task.url.substringAfterLast('/')
        val file = java.io.File(task.destDir, fileName)

        if (!file.exists()) return

        scope.launch {
            telegramUploader.upload(file)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DownloadRepository? = null

        fun getInstance(context: Context): DownloadRepository {
            return INSTANCE ?: synchronized(this) {
                val inst = DownloadRepository(context.applicationContext)
                INSTANCE = inst
                inst
            }
        }
    }
}