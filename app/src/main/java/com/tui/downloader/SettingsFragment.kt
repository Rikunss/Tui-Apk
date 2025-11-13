package com.tui.downloader

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.tui.downloader.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_prefs, rootKey)

        // Example: live summary update
        findPreference<EditTextPreference>("bot_token")?.apply {
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }

        findPreference<EditTextPreference>("chat_id")?.apply {
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }
    }
}