package by.liauko.siarhei.cl.activity.fragment

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
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.cl.util.DataPeriod
import by.liauko.siarhei.cl.util.ThemeMode

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var toolbar: Toolbar
    private lateinit var appContext: Context
    private lateinit var appVersion: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainScreenKey: String
    private lateinit var themeKey: String
    private lateinit var periodKey: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolbar = (container!!.parent as ViewGroup).getChildAt(0) as Toolbar
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)

        appContext = requireContext()
        appVersion = appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
        sharedPreferences = appContext.getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)

        mainScreenKey = getString(R.string.main_screen_key)
        themeKey = getString(R.string.theme_key)
        periodKey = getString(R.string.period_key)

        findPreference<ListPreference>(mainScreenKey)!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<ListPreference>(themeKey)!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<ListPreference>(periodKey)!!.onPreferenceChangeListener = preferenceChangeListener
        findPreference<Preference>("version")!!.summary = appVersion
        findPreference<Preference>("feedback")!!.setOnPreferenceClickListener {
            sendFeedback()
            true
        }
    }

    override fun onStart() {
        super.onStart()

        toolbar.navigationIcon = null
        toolbar.title = getString(R.string.settings_fragment_title)
        toolbar.menu.findItem(R.id.period_select_menu_date).isVisible = false
        toolbar.menu.findItem(R.id.car_profile_menu).isVisible = false
        toolbar.menu.findItem(R.id.export_to_pdf).isVisible = false
    }

    private val preferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        when (preference.key) {
            mainScreenKey -> sharedPreferences.edit()
                .putString(mainScreenKey, newValue.toString())
                .apply()

            themeKey -> {
                val mode = when (ThemeMode.valueOf(newValue.toString())) {
                    ThemeMode.SYSTEM -> if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                        else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                }
                sharedPreferences.edit()
                    .putInt(themeKey, mode)
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
