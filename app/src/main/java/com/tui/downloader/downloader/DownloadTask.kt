package com.tui.downloader.downloader

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.File
import java.util.UUID

/**
 * DownloadTask mewakili satu download item di queue.
 * Mendukung segmented download jika server mengizinkan (Accept-Ranges: bytes).
 */
class DownloadTask(
    val url: String,
    private val destDir: String,
    private val callback: () -> Unit
) {
    val id: String = UUID.randomUUID().toString()

    @Volatile var downloadedBytes: Long = 0L
    @Volatile var totalBytes: Long = 0L
    @Volatile var isPaused: Boolean = false
    @Volatile var isDone: Boolean = false
    @Volatile var isError: Boolean = false

    private val client = OkHttpClient.Builder().build()
    private val downloader = SegmentedDownloader(client, connections = 4)

    val statusText: String
        get() = when {
            isDone -> "Completed"
            isPaused -> "Paused"
            isError -> "Error"
            totalBytes > 0 -> "Downloading ${progressPercent}%"
            else -> "Starting..."
        }

    val progressPercent: Int
        get() = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0

    suspend fun start() = withContext(Dispatchers.IO) {
        try {
            val fileName = url.substringAfterLast('/')
            val destFile = File(destDir, fileName)

            val ok = downloader.download(url, destFile) { downloaded, total ->
                downloadedBytes = downloaded
                totalBytes = total
                callback()
                checkPaused()
            }

            isDone = ok && !isPaused
            callback()
        } catch (e: Exception) {
            e.printStackTrace()
            isError = true
            callback()
        }
    }

    private fun checkPaused() {
        if (isPaused) throw CancellationException("Task paused")
    }

    fun pause() {
        isPaused = true
    }
}
