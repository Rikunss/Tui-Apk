package com.tui.downloader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tui.downloader.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load preference fragment
        supportFragmentManager
            .beginTransaction()
            .replace(binding.settingsContainer.id, SettingsFragment())
            .commit()
    }
}