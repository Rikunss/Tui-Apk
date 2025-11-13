package com.tui.downloader

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tui.downloader.databinding.ItemDownloadBinding
import com.tui.downloader.downloader.DownloadRepository
import com.tui.downloader.downloader.DownloadTask

class DownloadListAdapter(private val repo: DownloadRepository) :
    RecyclerView.Adapter<DownloadListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = repo.tasks[position]
        val b = holder.binding

        b.txtUrl.text = task.url
        b.txtStatus.text = task.statusText
        b.progressBar.progress = task.progressPercent

        // Upload to Telegram
        b.btnUpload.setOnClickListener {
            repo.uploadToTelegram(task)
        }

        // Pause
        b.btnPause.setOnClickListener {
            repo.pause(task)
        }

        // Resume
        b.btnResume.setOnClickListener {
            repo.resume(task)
        }
    }

    override fun getItemCount(): Int = repo.tasks.size
}
