package by.liauko.siarhei.fcc.fragment

import android.Manifest
import android.accounts.Account
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.drive.DriveServiceHelper
import by.liauko.siarhei.fcc.synchronization.BackupUtil
import by.liauko.siarhei.fcc.util.AppResultCodes.getAccountsPermission
import by.liauko.siarhei.fcc.util.AppResultCodes.googleSignIn
import by.liauko.siarhei.fcc.util.AppResultCodes.internetPermission
import by.liauko.siarhei.fcc.util.AppResultCodes.userRecoverableAuth
import by.liauko.siarhei.fcc.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.fcc.util.DataPeriod
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

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
    private lateinit var exportFilePreference: Preference
    private lateinit var importFilePreference: Preference
    private lateinit var backupAccountPreference: Preference

    private var driveServiceHelper: DriveServiceHelper? = null

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
//        backupAccountPreference = findPreference(getString(R.string.backup_account_key))!!
//        backupAccountPreference.isEnabled = backupSwitcher.isChecked

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
//                backupAccountPreference.isEnabled = newValue

                if (newValue) {
                    googleAuth()
                } else {
                    getGoogleSignInClient().signOut().addOnSuccessListener {
                        driveServiceHelper = null
                    }
                }
            }
        }

        true
    }

    private val preferenceClickListener = Preference.OnPreferenceClickListener {
        when {
            it.key == "export_key" -> {
                try {
                    if (driveServiceHelper == null) {
                        googleAuth()
                    }
                    BackupUtil.exportData(appContext, driveServiceHelper!!)
                } catch (e: UserRecoverableAuthIOException) {
                    startActivityForResult(e.intent, userRecoverableAuth)
                }
            }
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

    private fun checkPermissions() {
        val internetPermission = ActivityCompat.checkSelfPermission(appContext, Manifest.permission.INTERNET)
        val accountPermission = ActivityCompat.checkSelfPermission(appContext, Manifest.permission.GET_ACCOUNTS)
        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf(Manifest.permission.INTERNET),
                internetPermission
            )
        }
        if (accountPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf(Manifest.permission.GET_ACCOUNTS),
                getAccountsPermission
            )
        }
    }

    private fun googleAuth() {
        checkPermissions()

        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(appContext)
        if (googleSignInAccount == null) {
            startActivityForResult(getGoogleSignInClient().signInIntent, googleSignIn)
        } else {
            driveServiceHelper = initDriveServiceHelper(googleSignInAccount.account)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == googleSignIn && resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnSuccessListener {
                driveServiceHelper = initDriveServiceHelper(it.account)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            internetPermission -> {
                if (grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(appContext, "Access to the internet is required", Toast.LENGTH_LONG).show()
                    disableSyncPreferenceItems()
                }
            }
            getAccountsPermission -> {
                if (grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(appContext, "Access to the accounts is required", Toast.LENGTH_LONG).show()
                    disableSyncPreferenceItems()
                }
            }
        }
    }

    private fun disableSyncPreferenceItems() {
        backupSwitcher.isChecked = false
        backupFrequencyPreference.isEnabled = false
    }

    private fun initDriveServiceHelper(account: Account?): DriveServiceHelper {
        val credential = GoogleAccountCredential.usingOAuth2(appContext, listOf(DriveScopes.DRIVE))
        credential.selectedAccount = account
        val googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),GsonFactory(),
            credential
        ).setApplicationName(appContext.getString(R.string.app_name)).build()

        return DriveServiceHelper(googleDriveService)
    }

    private fun getGoogleSignInClient(): GoogleSignInClient {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    Scope(Scopes.PROFILE),
                    Scope("https://www.googleapis.com/auth/drive.file")
                )
                .build()
        return GoogleSignIn.getClient(appContext, googleSignInOptions)
    }
}