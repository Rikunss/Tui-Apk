package com.tui.downloader.util

import android.content.Context import android.os.Build import android.os.Environment import android.provider.DocumentsContract import java.io.File

/**

Helper untuk operasi storage.

Versi sekarang fokus ke penyimpanan internal (aman dan tidak perlu SAF).

Kalau ingin support Android 11+ untuk External dir, bisa ditambah SAF. */ object StorageHelper {

/**

Mengembalikan folder download internal aplikasi. */ fun getInternalDownloadDir(context: Context): File { val dir = File(context.filesDir, "downloads") if (!dir.exists()) dir.mkdirs() return dir }


/**

Mendapatkan nama file dari URL. */ fun extractFileName(url: String): String { return url.substringAfterLast('/', fallback = "file.bin") }


private fun String.substringAfterLast(delim: Char, fallback: String): String { val idx = this.lastIndexOf(delim) return if (idx == -1) fallback else this.substring(idx + 1) } }