package src.cordova.plugin.videocall.AudioSettingsFragment

import android.os.Bundle
import io.ionic.starter.R
import src.cordova.plugin.videocall.BaseSettingsFragment.BaseSettingsFragment


class AudioSettingsFragment : BaseSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.audio_preferences)

        setHasOptionsMenu(true)
    }
}
