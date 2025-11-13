package com.tui.downloader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tui.downloader.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load existing values
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        binding.editBotToken.setText(prefs.getString("bot_token", ""))
        binding.editChatId.setText(prefs.getString("chat_id", ""))

        // Save button
        binding.btnSave.setOnClickListener {
            val token = binding.editBotToken.text.toString().trim()
            val chatId = binding.editChatId.text.toString().trim()

            prefs.edit()
                .putString("bot_token", token)
                .putString("chat_id", chatId)
                .apply()

            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}