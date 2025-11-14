package com.tui.downloader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.tui.downloader.databinding.ActivityMainBinding
import com.tui.downloader.downloader.DownloadManagerService
import com.tui.downloader.downloader.DownloadRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val scope = MainScope()
    private val repo by lazy { DownloadRepository.getInstance(applicationContext) }

    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Storage permission needed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        checkStoragePermission()
        setupRecycler()
        setupButtons()

        repo.onChange = {
            scope.launch {
                binding.recycler.adapter?.notifyDataSetChanged()
            }
        }
    }

    // ============================================================
    // STORAGE PERMISSION HANDLER
    // ============================================================
    private fun checkStoragePermission() {
        when {
            Build.VERSION.SDK_INT >= 33 -> {
                val perm = Manifest.permission.READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(perm)
                }
            }

            Build.VERSION.SDK_INT >= 30 -> {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
            }

            else -> {
                val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(perm)
                }
            }
        }
    }

    // ============================================================
    // RECYCLER
    // ============================================================
    private fun setupRecycler() {
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = DownloadListAdapter(repo)
    }

    // ============================================================
    // BUTTON HANDLER
    // ============================================================
    private fun setupButtons() {

        // ADD DOWNLOAD
        binding.btnAdd.setOnClickListener {
            val url = binding.inputUrl.text.toString().trim()

            if (url.isEmpty()) {
                Toast.makeText(this, "URL kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Default folder
            val downloadPath = "/storage/emulated/0/Download/Tui Downloader/"
            val folder = File(downloadPath)
            if (!folder.exists()) folder.mkdirs()

            try {
                repo.createDownload(url, downloadPath)
                binding.recycler.adapter?.notifyDataSetChanged()

                startService(Intent(this, DownloadManagerService::class.java))

                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error adding download", Toast.LENGTH_SHORT).show()
            }
        }

        // SETTINGS PAGE
        binding.btnMenu.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
