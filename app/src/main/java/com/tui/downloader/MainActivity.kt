package com.tui.downloader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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

class MainActivity : AppCompatActivity() { private lateinit var binding: ActivityMainBinding private val scope = MainScope() private val repo by lazy { DownloadRepository.getInstance(applicationContext) }

private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (!granted) Toast.makeText(this, "Storage permission needed", Toast.LENGTH_SHORT).show()
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Load saved dark mode state
    val prefs = getSharedPreferences("settings", MODE_PRIVATE)
    val savedMode = prefs.getInt("dark_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(savedMode)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    checkStoragePermission()

    setupRecycler()
    setupButtons()

    // Dark mode toggle
    binding.btnDarkMode.setOnClickListener {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val current = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode()
        val newMode = if (current == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        } else {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
        }
        // Apply
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(newMode)
        // Save
        prefs.edit().putInt("dark_mode", newMode).apply()
    } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    // observe download list changes
    repo.onChange = {
        scope.launch { binding.recycler.adapter?.notifyDataSetChanged() }
    }
}

private fun checkStoragePermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED
    ) {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}

private fun setupRecycler() {
    binding.recycler.layoutManager = LinearLayoutManager(this)
    val adapter = com.tui.downloader.DownloadListAdapter(repo)
    binding.recycler.adapter = adapter
}

private fun setupButtons() {
    binding.btnAdd.setOnClickListener {
        val url = binding.inputUrl.text.toString().trim()
        if (url.isNotEmpty()) {
            repo.createDownload(url, filesDir.absolutePath)
            binding.recycler.adapter?.notifyDataSetChanged()
            startService(Intent(this, DownloadManagerService::class.java))
        }
    }

    binding.btnSettings.setOnClickListener {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}

}