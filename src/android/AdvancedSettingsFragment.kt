package src.cordova.plugin.videocall.AdvancedSettingsFragment


import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.twilio.video.*
import io.ionic.starter.R
import src.cordova.plugin.videocall.BaseSettingsFragment.BaseSettingsFragment
import src.cordova.plugin.videocall.BuildConfigUtils.isInternalFlavor
import src.cordova.plugin.videocall.Preferences.Preferences
import src.cordova.plugin.videocall.Preferences.Preferences.VIDEO_CAPTURE_RESOLUTION
import src.cordova.plugin.videocall.Preferences.Preferences.VIDEO_DIMENSIONS

class AdvancedSettingsFragment : BaseSettingsFragment() {

    private var identityPreference: EditTextPreference? = null
    private val videoCodecNames = arrayOf(Vp8Codec.NAME, H264Codec.NAME, Vp9Codec.NAME)
    private val audioCodecNames = arrayOf(IsacCodec.NAME, OpusCodec.NAME, PcmaCodec.NAME, PcmuCodec.NAME, G722Codec.NAME)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.advanced_preferences)

        findPreference<Preference>(Preferences.INTERNAL)?.isVisible = isInternalFlavor

        setHasOptionsMenu(true)

        (findPreference(VIDEO_CAPTURE_RESOLUTION) as ListPreference?)?.run {
            entries = VIDEO_DIMENSIONS.map { "${it.width}x${it.height}" }.toTypedArray()
            entryValues = (0..VIDEO_DIMENSIONS.lastIndex).map { it.toString() }.toTypedArray()
            setSummaryProvider { preference ->
                (preference as ListPreference).let { listPreference ->
                    VIDEO_DIMENSIONS[listPreference.value.toInt()].let { dimensions ->
                        entries.find { it == "${dimensions.width}x${dimensions.height}" }
                    }
                }
            }
        }

        setupCodecListPreference(
                VideoCodec::class.java,
                Preferences.VIDEO_CODEC,
                Preferences.VIDEO_CODEC_DEFAULT,
                findPreference(Preferences.VIDEO_CODEC) as ListPreference?)

        setupCodecListPreference(
                AudioCodec::class.java,
                Preferences.AUDIO_CODEC,
                Preferences.AUDIO_CODEC_DEFAULT,
                findPreference(Preferences.AUDIO_CODEC) as ListPreference?)

        // Fill out the rest of settings
        identityPreference = findPreference<EditTextPreference>(Preferences.DISPLAY_NAME)?.apply {
            summary = sharedPreferences.getString(Preferences.DISPLAY_NAME, null)
            setOnPreferenceChangeListener { _, newValue ->
                summary = newValue as CharSequence?
                true
            }
        }
    }

    private fun setupCodecListPreference(
        codecClass: Class<*>,
        key: String,
        defaultValue: String,
        preference: ListPreference?
    ) {
        if (preference == null) {
            return
        }

        val codecEntries = if (codecClass == AudioCodec::class.java) audioCodecNames else videoCodecNames
        // saved value
        val value = sharedPreferences.getString(key, defaultValue)

        // bind values
        preference.entries = codecEntries
        preference.entryValues = codecEntries
        preference.value = value
        preference.summary = value
        preference.setOnPreferenceChangeListener { changedPreference, newValue ->
            changedPreference.summary = newValue.toString()
            true
        }
    }
}
