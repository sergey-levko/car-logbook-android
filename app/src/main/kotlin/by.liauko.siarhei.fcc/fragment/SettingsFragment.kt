package by.liauko.siarhei.fcc.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import by.liauko.siarhei.fcc.R

class SettingsFragment: PreferenceFragmentCompat() {
    private val mainScreenKey = "type"

    private lateinit var appContext: Context
    private lateinit var appVersion: String
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)

        appContext = requireContext()
        appVersion = appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
        sharedPreferences = appContext.getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)

        findPreference<DropDownPreference>("main_page")!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<Preference>("version")!!.summary = appVersion
        findPreference<Preference>("feedback")!!.setOnPreferenceClickListener {
            sendFeedback()
            true
        }
    }

    private val preferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        val value = newValue.toString()
        if (preference is DropDownPreference) {
            sharedPreferences.edit()
                .putString(mainScreenKey, value)
                .apply()
        }

        true
    }

    private fun sendFeedback() {
        val body = """|
            |
            |------------------------
            |${getString(R.string.settings_feedback_email_dont_remove)}
            |${getString(R.string.settings_feedback_email_os)} ${Build.VERSION.RELEASE}
            |${getString(R.string.settings_feedback_email_app_version)} $appVersion
            |${getString(R.string.settings_feedback_email_device)} ${Build.MANUFACTURER} ${Build.MODEL}
        """.trimMargin()

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("okvel.s@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_feedback_email_subject))
        intent.putExtra(Intent.EXTRA_TEXT, body)
        startActivity(Intent.createChooser(intent, getString(R.string.settings_feedback_email_client)))
    }
}