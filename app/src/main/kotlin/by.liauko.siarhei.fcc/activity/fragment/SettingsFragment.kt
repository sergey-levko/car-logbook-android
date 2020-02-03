package by.liauko.siarhei.fcc.activity.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.fcc.util.DataPeriod

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var appContext: Context
    private lateinit var appVersion: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainScreenKey: String
    private lateinit var darkModeKey: String
    private lateinit var periodKey: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val toolbar = (container!!.parent as ViewGroup).getChildAt(0) as Toolbar
        toolbar.navigationIcon = null
        toolbar.title = getString(R.string.settings_fragment_title)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)

        appContext = requireContext()
        appVersion = appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
        sharedPreferences = appContext.getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)

        mainScreenKey = getString(R.string.main_screen_key)
        darkModeKey = getString(R.string.dark_mode_key)
        periodKey = getString(R.string.period_key)

        findPreference<ListPreference>(mainScreenKey)!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<SwitchPreference>(darkModeKey)!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<ListPreference>(periodKey)!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<Preference>("version")!!.summary = appVersion
        findPreference<Preference>("feedback")!!.setOnPreferenceClickListener {
            sendFeedback()
            true
        }
    }

    private val preferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        when (preference.key) {
            mainScreenKey -> sharedPreferences.edit()
                .putString(mainScreenKey, newValue.toString())
                .apply()

            darkModeKey -> {
                newValue as Boolean
                val mode = if (newValue) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                sharedPreferences.edit()
                    .putInt(darkModeKey, mode)
                    .apply()

                AppCompatDelegate.setDefaultNightMode(mode)
            }
            periodKey -> {
                sharedPreferences.edit()
                    .putString(periodKey, newValue.toString())
                    .apply()
                dataPeriod = DataPeriod.valueOf(newValue.toString())
            }
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
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("okvel.work@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_feedback_email_subject))
        intent.putExtra(Intent.EXTRA_TEXT, body)
        startActivity(Intent.createChooser(intent, getString(R.string.settings_feedback_email_client)))
    }
}