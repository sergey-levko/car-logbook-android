package by.liauko.siarhei.fcc.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.synchronization.BackupUtil
import by.liauko.siarhei.fcc.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.fcc.util.DataPeriod

class SettingsFragment: PreferenceFragmentCompat() {
    private lateinit var appContext: Context
    private lateinit var appVersion: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainScreenKey: String
    private lateinit var themeKey: String
    private lateinit var periodKey: String
    private lateinit var backupSwitcherKey: String
    private lateinit var backupSwitcher: SwitchPreference
    private lateinit var backupFrequencyPreference: ListPreference
    private lateinit var backupAccountPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)

        appContext = requireContext()
        appVersion = appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
        sharedPreferences = appContext.getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)

        mainScreenKey = getString(R.string.main_screen_key)
        themeKey = getString(R.string.theme_key)
        periodKey = getString(R.string.period_key)
        backupSwitcherKey = getString(R.string.backup_key)

        findPreference<ListPreference>(mainScreenKey)!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<ListPreference>(themeKey)!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<ListPreference>(periodKey)!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<Preference>("version")!!.summary = appVersion
        findPreference<Preference>("feedback")!!.setOnPreferenceClickListener {
            sendFeedback()
            true
        }
        backupSwitcher = findPreference(backupSwitcherKey)!!
        backupSwitcher.onPreferenceChangeListener = preferenceChangeListener
        backupFrequencyPreference = findPreference(getString(R.string.backup_frequency_key))!!
        backupFrequencyPreference.isEnabled = backupSwitcher.isChecked
        backupAccountPreference = findPreference(getString(R.string.backup_account_key))!!
        backupAccountPreference.isEnabled = backupSwitcher.isChecked

        findPreference<Preference>("export_key")!!.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>("import_key")!!.onPreferenceClickListener = preferenceClickListener
    }

    private val preferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        when {
            preference.key == mainScreenKey -> sharedPreferences.edit()
                .putString(mainScreenKey, newValue.toString())
                .apply()
            preference.key == themeKey -> {
                sharedPreferences.edit()
                    .putString(themeKey, newValue.toString())
                    .apply()
                requireActivity().finish()
                startActivity(requireActivity().intent)
            }
            preference.key == periodKey -> {
                sharedPreferences.edit()
                    .putString(periodKey, newValue.toString())
                    .apply()
                dataPeriod = DataPeriod.valueOf(newValue.toString())
            }
            preference.key == backupSwitcherKey -> {
                newValue as Boolean
                backupFrequencyPreference.isEnabled = newValue
                backupAccountPreference.isEnabled = newValue
            }
        }

        true
    }

    private val preferenceClickListener = Preference.OnPreferenceClickListener {
        when {
            it.key == "export_key" -> BackupUtil.exportData(appContext)
            it.key == "import_key" -> BackupUtil.importData(appContext)
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