package com.tui.downloader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.tui.downloader.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    // 🔥 DITAMBAHKAN — Launcher untuk pilih folder
    private val folderPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                prefs.edit().putString("download_folder_uri", uri.toString()).apply()

                // Ambil izin permanen
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                Toast.makeText(this, "Folder berhasil disimpan!", Toast.LENGTH_SHORT).show()
                updateFolderStatus() // refresh status
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✔ SUDAH ADA — Load bot token & chat ID
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        binding.editBotToken.setText(prefs.getString("bot_token", ""))
        binding.editChatId.setText(prefs.getString("chat_id", ""))

        // ✔ SUDAH ADA — Tombol Save
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

        // 🔥 DITAMBAHKAN — Tombol pilih folder download
        binding.btnPickFolder.setOnClickListener {
            folderPicker.launch(null)
        }

        // 🔥 DITAMBAHKAN — Update status folder
        updateFolderStatus()
    }

    // 🔥 DITAMBAHKAN — Menampilkan status (✔ atau ❌)
    private fun updateFolderStatus() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val uri = prefs.getString("download_folder_uri", null)

        if (uri != null) {
            binding.txtFolderStatus.text = "✔ Akses diberikan"
        } else {
            binding.txtFolderStatus.text = "❌ Belum diberi akses"
        }
    }
}
