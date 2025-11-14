package com.tui.downloader

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tui.downloader.downloader.DownloadRepository
import com.tui.downloader.downloader.DownloadTask

class DownloadListAdapter(
    private val repo: DownloadRepository
) : RecyclerView.Adapter<DownloadListAdapter.DownloadViewHolder>() {

    class DownloadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtFileName: TextView = view.findViewById(R.id.txtFileName)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val progressCircle: ImageView = view.findViewById(R.id.progressCircle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_download, parent, false)
        return DownloadViewHolder(view)
    }

    override fun getItemCount(): Int = repo.tasks.size

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        val task = repo.tasks[position]

        // Set file name
        val fileName = task.url.substringAfterLast("/")
        holder.txtFileName.text = fileName

        // Progress
        holder.progressBar.max = 100
        holder.progressBar.progress = task.progressPercent

        // Draw circular progress percentage
        holder.progressCircle.setImageBitmap(
            generateProgressBitmap(task.progressPercent)
        )

        // Click → pause/resume
        holder.progressCircle.setOnClickListener {
            if (task.isPaused) repo.resume(task)
            else repo.pause(task)

            notifyItemChanged(position)
        }

        // Long click → upload
        holder.progressCircle.setOnLongClickListener {
            repo.uploadToTelegram(task)
            true
        }
    }

    // ============================================
    // Generate circle percentage bitmap
    // ============================================
    private fun generateProgressBitmap(percent: Int): Bitmap {
        val size = 44
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Background circle
        val bgPaint = Paint()
        bgPaint.color = Color.parseColor("#E0E0E0")
        bgPaint.isAntiAlias = true
        canvas.drawCircle(size/2f, size/2f, size/2f, bgPaint)

        // Progress arc
        val progressPaint = Paint()
        progressPaint.color = Color.parseColor("#7B61FF") // ungu
        progressPaint.isAntiAlias = true
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = 6f

        val rect = RectF(6f, 6f, size - 6f, size - 6f)
        val sweep = 360 * (percent / 100f)
        canvas.drawArc(rect, -90f, sweep, false, progressPaint)

        // Text (percentage)
        val textPaint = Paint()
        textPaint.color = Color.BLACK
        textPaint.textSize = 16f
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER

        val y = size / 2f - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText("$percent%", size/2f, y, textPaint)

        return bmp
    }
}
