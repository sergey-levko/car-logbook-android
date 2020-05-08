package by.liauko.siarhei.cl.activity.fragment

import android.Manifest
import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.dialog.DriveImportDialog
import by.liauko.siarhei.cl.backup.BackupService
import by.liauko.siarhei.cl.backup.CoroutineBackupWorker
import by.liauko.siarhei.cl.drive.DRIVE_ROOT_FOLDER_ID
import by.liauko.siarhei.cl.drive.DriveServiceHelper
import by.liauko.siarhei.cl.util.AppResultCodes
import by.liauko.siarhei.cl.util.ApplicationUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.concurrent.TimeUnit

class BackupSettingsFragment : PreferenceFragmentCompat() {

    private val workTag = "car-logbook-backup-work"

    private var driveServiceHelper: DriveServiceHelper? = null
    private var backupTask = BackupTask.IMPORT

    private lateinit var appContext: Context
    private lateinit var backupSwitcherKey: String
    private lateinit var backupFrequencyKey: String
    private lateinit var backupFileExportKey: String
    private lateinit var backupFileImportKey: String
    private lateinit var backupDriveImportKey: String
    private lateinit var backupResetKey: String
    private lateinit var backupSwitcher: SwitchPreference
    private lateinit var backupFrequencyPreference: ListPreference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val toolbar = (container!!.parent as ViewGroup).getChildAt(0) as Toolbar
        toolbar.title = getString(R.string.settings_preference_backup_title)
        toolbar.setNavigationIcon(R.drawable.arrow_left_white)
        toolbar.setNavigationOnClickListener {
            fragmentManager?.popBackStack()
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.backup_preference)

        appContext = requireContext()
        sharedPreferences = appContext.getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)

        backupSwitcherKey = getString(R.string.backup_switcher_key)
        backupFrequencyKey = getString(R.string.backup_frequency_key)
        backupFileExportKey = getString(R.string.backup_file_export_key)
        backupFileImportKey = getString(R.string.backup_file_import_key)
        backupDriveImportKey = getString(R.string.backup_drive_import_key)
        backupResetKey = getString(R.string.backup_reset_key)

        backupSwitcher = findPreference(backupSwitcherKey)!!
        backupSwitcher.onPreferenceClickListener = preferenceClickListener
        backupSwitcher.onPreferenceChangeListener = preferenceChangeListener
        backupFrequencyPreference = findPreference(backupFrequencyKey)!!
        backupFrequencyPreference.isEnabled = backupSwitcher.isChecked
        backupFrequencyPreference.onPreferenceChangeListener = preferenceChangeListener
        findPreference<Preference>(backupFileExportKey)!!.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>(backupFileImportKey)!!.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>(backupDriveImportKey)!!.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>(backupResetKey)!!.onPreferenceClickListener = preferenceClickListener
    }

    private val preferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        when (preference.key) {
            backupSwitcherKey -> {
                newValue as Boolean
                backupFrequencyPreference.isEnabled = newValue

                if (newValue) {
                    if (checkInternetConnection()) {
                        backupTask = BackupTask.EXPORT
                        googleAuth()
                    }
                } else {
                    WorkManager.getInstance(appContext).cancelAllWorkByTag(workTag)
                    getGoogleSignInClient().signOut().addOnSuccessListener {
                        driveServiceHelper = null
                    }
                }
            }
            backupFrequencyKey -> {
                val repeatInterval = newValue.toString().toLong()
                if (repeatInterval != 0L) {
                    startBackupWorker(repeatInterval)
                } else {
                    WorkManager.getInstance(appContext).cancelAllWorkByTag(workTag)
                }
            }
        }

        true
    }

    private val preferenceClickListener = Preference.OnPreferenceClickListener {
        when (it.key) {
            backupSwitcherKey -> {
                if (!checkInternetConnection()) {
                    Toast.makeText(
                        appContext,
                        R.string.settings_preference_backup_internet_access_toast_text,
                        Toast.LENGTH_LONG
                    ).show()
                    disableSyncPreferenceItems()
                }
            }
            backupDriveImportKey -> {
                if (!checkInternetConnection()) {
                    Toast.makeText(
                        appContext,
                        R.string.settings_preference_backup_internet_access_toast_text,
                        Toast.LENGTH_LONG
                    ).show()
                } else if (driveServiceHelper == null) {
                    backupTask = BackupTask.IMPORT
                    googleAuth()
                } else {
                    importDataFromDrive()
                }
            }
            backupFileExportKey ->
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), AppResultCodes.BACKUP_OPEN_DOCUMENT_TREE)
            backupFileImportKey -> {
                val openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                openDocumentIntent.addCategory(Intent.CATEGORY_OPENABLE)
                openDocumentIntent.type = "application/*"
                startActivityForResult(openDocumentIntent, AppResultCodes.BACKUP_OPEN_DOCUMENT)
            }
            backupResetKey -> {
                MaterialAlertDialogBuilder(appContext)
                    .setTitle(R.string.dialog_reset_title)
                    .setMessage(R.string.dialog_reset_message)
                    .setPositiveButton(R.string.yes) { dialog, _ ->
                        BackupService.eraseAllData(appContext)
                        dialog.dismiss()
                        Toast.makeText(appContext, R.string.dialog_reset_toast_message, Toast.LENGTH_LONG)
                            .show()
                    }
                    .setNegativeButton(R.string.no) { dialog, _ -> dialog.cancel() }
                    .show()
            }
        }

        true
    }

    private fun checkPermissions() {
        val internetPermission = ActivityCompat.checkSelfPermission(appContext, Manifest.permission.INTERNET)
        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf(Manifest.permission.INTERNET),
                internetPermission
            )
        }
    }

    private fun googleAuth() {
        checkPermissions()

        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(appContext)
        if (googleSignInAccount == null) {
            startActivityForResult(
                getGoogleSignInClient().signInIntent,
                AppResultCodes.GOOGLE_SIGN_IN
            )
        } else {
            executeBackupTask(googleSignInAccount.account)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            AppResultCodes.GOOGLE_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    task.addOnSuccessListener {
                        executeBackupTask(it.account)
                    }
                } else {
                    disableSyncPreferenceItems()
                }
            }
            AppResultCodes.BACKUP_OPEN_DOCUMENT_TREE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val progressDialog = ApplicationUtil.createProgressDialog(
                        appContext,
                        R.string.dialog_backup_progress_export_text
                    )
                    progressDialog.show()
                    BackupService.exportToFile(data?.data ?: Uri.EMPTY, appContext, progressDialog)
                }
            }
            AppResultCodes.BACKUP_OPEN_DOCUMENT -> {
                if (resultCode == Activity.RESULT_OK) {
                    val progressDialog = ApplicationUtil.createProgressDialog(
                        appContext,
                        R.string.dialog_backup_progress_import_text
                    )
                    progressDialog.show()
                    BackupService.importFromFile(data?.data ?: Uri.EMPTY, appContext, progressDialog)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == AppResultCodes.INTERNET_PERMISSION &&
            grantResults.isEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(
                appContext,
                getString(R.string.settings_preference_backup_internet_permission_toast_text),
                Toast.LENGTH_LONG
            ).show()
            disableSyncPreferenceItems()
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
            NetHttpTransport(),
            GsonFactory(),
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
                    Scope("https://www.googleapis.com/auth/drive")
                )
                .build()
        return GoogleSignIn.getClient(appContext, googleSignInOptions)
    }

    private fun exportDataToDrive() {
        val repeatInterval = backupFrequencyPreference.value.toLong()
        if (repeatInterval != 0L) {
            startBackupWorker(repeatInterval)
        }
    }

    private fun importDataFromDrive() {
        val progressDialog = ApplicationUtil.createProgressDialog(
            appContext,
            R.string.dialog_backup_progress_open_file_list
        )
        progressDialog.show()

        var folderId = DRIVE_ROOT_FOLDER_ID
        driveServiceHelper!!.getFolderIdByName("car-logbook-backup")
            .addOnCompleteListener { searchResult ->
                folderId = searchResult.result ?: folderId
            }.continueWithTask {
                driveServiceHelper!!.getAllFilesInFolderTask(folderId)
                    .addOnCompleteListener { fileList ->
                        val files = fileList.result ?: ArrayList()
                        val driveImportDialog = DriveImportDialog(appContext, driveServiceHelper!!, files)
                        progressDialog.dismiss()
                        driveImportDialog.show()
                    }
            }
    }

    private fun startBackupWorker(repeatInterval: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<CoroutineBackupWorker>(repeatInterval, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(appContext)
            .enqueueUniquePeriodicWork(workTag, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
    }

    // NetworkInfo class is deprecated in Android 10
    @Suppress("DEPRECATION")
    private fun checkInternetConnection(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true
    }

    private fun executeBackupTask(account: Account?) {
        driveServiceHelper = initDriveServiceHelper(account)
        when (backupTask) {
            BackupTask.IMPORT -> importDataFromDrive()
            BackupTask.EXPORT -> exportDataToDrive()
        }
    }
}

enum class BackupTask {
    IMPORT, EXPORT
}