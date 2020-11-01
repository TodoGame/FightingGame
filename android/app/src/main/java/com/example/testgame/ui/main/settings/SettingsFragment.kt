package com.example.testgame.ui.main.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.testgame.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}