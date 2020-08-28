package by.liauko.siarhei.cl.activity.fragment

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.work.WorkManager
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.backup.BackupService
import by.liauko.siarhei.cl.backup.BackupTask
import by.liauko.siarhei.cl.backup.PermissionService
import by.liauko.siarhei.cl.backup.adapter.toBackupAdapter
import by.liauko.siarhei.cl.util.AppResultCodes.BACKUP_OPEN_DOCUMENT
import by.liauko.siarhei.cl.util.AppResultCodes.BACKUP_OPEN_DOCUMENT_TREE
import by.liauko.siarhei.cl.util.AppResultCodes.GOOGLE_SIGN_IN
import by.liauko.siarhei.cl.util.AppResultCodes.INTERNET_PERMISSION
import by.liauko.siarhei.cl.util.ImportFromFileAsyncTask
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BackupSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var appContext: Context
    private lateinit var backupSwitcherKey: String
    private lateinit var backupFrequencyKey: String
    private lateinit var backupFileExportKey: String
    private lateinit var backupFileImportKey: String
    private lateinit var backupDriveExportKey: String
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
            parentFragmentManager.popBackStack()
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
        backupDriveExportKey = getString(R.string.backup_drive_export_key)
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
        findPreference<Preference>(backupDriveExportKey)!!.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>(backupDriveImportKey)!!.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>(backupResetKey)!!.onPreferenceClickListener = preferenceClickListener
    }

    private val preferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        when (preference.key) {
            backupSwitcherKey -> {
                newValue as Boolean
                backupFrequencyPreference.isEnabled = newValue

                if (newValue) {
                    if (PermissionService.checkInternetConnection(appContext)) {
                        BackupService.backupTask = BackupTask.EXPORT
                        BackupService.repeatInterval = backupFrequencyPreference.value.toLong()
                        BackupService.googleAuth(this.toBackupAdapter())
                    }
                } else {
                    WorkManager.getInstance(appContext).cancelAllWorkByTag(BackupService.WORK_TAG)
                    BackupService.getGoogleSignInClient(appContext).signOut().addOnSuccessListener {
                        BackupService.driveServiceHelper = null
                    }
                }
            }
            backupFrequencyKey -> {
                val repeatInterval = newValue.toString().toLong()
                BackupService.repeatInterval = repeatInterval
                if (repeatInterval != 0L) {
                    BackupService.startBackupWorker(appContext)
                } else {
                    WorkManager.getInstance(appContext).cancelAllWorkByTag(BackupService.WORK_TAG)
                }
            }
        }

        true
    }

    private val preferenceClickListener = Preference.OnPreferenceClickListener {
        when (it.key) {
            backupSwitcherKey -> {
                if (!PermissionService.checkInternetConnection(appContext)) {
                    Toast.makeText(
                        appContext,
                        R.string.settings_preference_backup_internet_access_toast_text,
                        Toast.LENGTH_LONG
                    ).show()
                    disableSyncPreferenceItems()
                }
            }
            backupDriveExportKey -> {
                if (!PermissionService.checkInternetConnection(appContext)) {
                    Toast.makeText(
                        appContext,
                        R.string.settings_preference_backup_internet_access_toast_text,
                        Toast.LENGTH_LONG
                    ).show()
                } else if (BackupService.driveServiceHelper == null) {
                    BackupService.backupTask = BackupTask.MANUAL_EXPORT
                    BackupService.googleAuth(this.toBackupAdapter())
                } else {
                    BackupService.exportToDrive(appContext)
                }
            }
            backupDriveImportKey -> {
                if (!PermissionService.checkInternetConnection(appContext)) {
                    Toast.makeText(
                        appContext,
                        R.string.settings_preference_backup_internet_access_toast_text,
                        Toast.LENGTH_LONG
                    ).show()
                } else if (BackupService.driveServiceHelper == null) {
                    BackupService.backupTask = BackupTask.IMPORT
                    BackupService.googleAuth(this.toBackupAdapter())
                } else {
                    BackupService.importDataFromDrive(appContext, null)
                }
            }
            backupFileExportKey ->
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), BACKUP_OPEN_DOCUMENT_TREE)
            backupFileImportKey -> {
                BackupService.openDocument(this.toBackupAdapter())
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GOOGLE_SIGN_IN -> {
                if (resultCode == RESULT_OK) {
                    BackupService.googleSignInResult(appContext, data, null)
                } else {
                    disableSyncPreferenceItems()
                }
            }
            BACKUP_OPEN_DOCUMENT_TREE -> {
                if (resultCode == RESULT_OK) {
                    BackupService.exportToFile(data?.data ?: Uri.EMPTY, appContext)
                }
            }
            BACKUP_OPEN_DOCUMENT -> {
                if (resultCode == RESULT_OK) {
                    ImportFromFileAsyncTask(data?.data ?: Uri.EMPTY, appContext, null).execute()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == INTERNET_PERMISSION &&
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
}
