package com.tui.downloader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Storage permission needed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Tui Downloader"

        checkStoragePermission()
        setupRecycler()
        setupButtons()
        setupDarkModeToggle()

        // Observe changes
        repo.onChange = {
            scope.launch { binding.recycler.adapter?.notifyDataSetChanged() }
        }
    }

    // ===============================
    // ACTIONBAR
    // ===============================

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ===============================
    // OTHER FUNCTIONS
    // ===============================

    private fun setupDarkModeToggle() {
        binding.btnDarkMode.setOnClickListener {
            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            val current = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode()

            val newMode =
                if (current == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                else
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES

            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(newMode)
            prefs.edit().putInt("dark_mode", newMode).apply()
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun setupRecycler() {
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = DownloadListAdapter(repo)
    }

    // ===============================
    // BUTTON ADD DOWNLOAD
    // ===============================

    private fun setupButtons() {
        binding.btnAdd.setOnClickListener {

            val url = binding.inputUrl.text.toString().trim()
            if (url.isEmpty()) return@setOnClickListener

            // Path download public
            val downloadPath = "/storage/emulated/0/Download/Tui Downloader/"

            val folder = File(downloadPath)
            if (!folder.exists()) folder.mkdirs()

            repo.createDownload(url, downloadPath)

            binding.recycler.adapter?.notifyDataSetChanged()

            startService(Intent(this, DownloadManagerService::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
