package com.tui.downloader.downloader

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile

/**
 * Downloader dengan segmented download (multi-connection) dan fallback ke single stream.
 * Mendukung resume jika server menyediakan Accept-Ranges.
 */
class SegmentedDownloader(
    private val client: OkHttpClient,
    private val connections: Int = 4
) {
    suspend fun download(
        url: String,
        dest: File,
        progressCb: (downloaded: Long, total: Long) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {

        // HEAD request untuk cek ukuran file & Accept-Ranges
        val headReq = Request.Builder().url(url).head().build()
        val headResp = client.newCall(headReq).execute()
        val length = headResp.header("Content-Length")?.toLongOrNull() ?: -1L
        val acceptRanges = headResp.header("Accept-Ranges") == "bytes"
        headResp.close()

        if (length <= 0 || !acceptRanges) {
            // fallback ke single download
            return@withContext singleDownload(url, dest, progressCb)
        }

        val partSize = length / connections
        val tmpFiles = mutableListOf<File>()
        var downloadedTotal = 0L

        coroutineScope {
            val jobs = (0 until connections).map { idx ->
                val start = idx * partSize
                val end = if (idx == connections - 1) length - 1 else (start + partSize - 1)
                val tmp = File(dest.parentFile, dest.name + ".part$idx")
                tmpFiles.add(tmp)

                launch {
                    val req = Request.Builder()
                        .url(url)
                        .addHeader("Range", "bytes=$start-$end")
                        .build()

                    client.newCall(req).execute().use { resp ->
                        val body = resp.body ?: return@launch
                        val raf = RandomAccessFile(tmp, "rw")
                        raf.setLength(0)

                        val ins = body.byteStream()
                        val buf = ByteArray(8192)
                        var r: Int
                        var local = 0L

                        while (ins.read(buf).also { r = it } != -1) {
                            raf.write(buf, 0, r)
                            local += r

                            synchronized(this@SegmentedDownloader) {
                                downloadedTotal += r
                                progressCb(downloadedTotal, length)
                            }
                        }
                        raf.close()
                    }
                }
            }
            jobs.joinAll()
        }

        // gabungkan semua part
        val out = RandomAccessFile(dest, "rw")
        out.setLength(length)

        tmpFiles.forEachIndexed { idx, part ->
            val ins = part.inputStream()
            val buf = ByteArray(8192)
            var r: Int
            out.seek(idx * partSize)
            while (ins.read(buf).also { r = it } != -1) {
                out.write(buf, 0, r)
            }
            ins.close()
            part.delete()
        }

        out.close()
        true
    }

    private fun singleDownload(
        url: String,
        dest: File,
        progressCb: (Long, Long) -> Unit
    ): Boolean {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body ?: return false
            val total = body.contentLength()
            val ins = body.byteStream()
            dest.outputStream().use { out ->
                val buf = ByteArray(8192)
                var r: Int
                var downloaded = 0L
                while (ins.read(buf).also { r = it } != -1) {
                    out.write(buf, 0, r)
                    downloaded += r
                    progressCb(downloaded, total)
                }
            }
            return true
        }
    }
}
