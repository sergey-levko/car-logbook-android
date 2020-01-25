package by.liauko.siarhei.fcc.fragment

import android.Manifest
import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.activity.dialog.DriveImportDialog
import by.liauko.siarhei.fcc.backup.BackupUtil
import by.liauko.siarhei.fcc.drive.DriveServiceHelper
import by.liauko.siarhei.fcc.util.AppResultCodes
import by.liauko.siarhei.fcc.util.ApplicationUtil
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

class BackupSettingsFragment: PreferenceFragmentCompat() {

    companion object {
        var driveServiceHelper: DriveServiceHelper? = null
    }

    private lateinit var appContext: Context
    private lateinit var backupSwitcherKey: String
    private lateinit var backupSwitcher: SwitchPreference
    private lateinit var backupFrequencyPreference: ListPreference

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

        backupSwitcherKey = getString(R.string.backup_key)
        backupSwitcher = findPreference(backupSwitcherKey)!!
        backupSwitcher.onPreferenceChangeListener = preferenceChangeListener
        backupFrequencyPreference = findPreference(getString(R.string.backup_frequency_key))!!
        backupFrequencyPreference.isEnabled = backupSwitcher.isChecked
        findPreference<Preference>(getString(R.string.backup_export_key))!!.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>(getString(R.string.backup_file_import))!!.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>(getString(R.string.backup_drive_import))!!.onPreferenceClickListener = preferenceClickListener
    }

    private val preferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        when (preference.key) {
            backupSwitcherKey -> {
                newValue as Boolean
                backupFrequencyPreference.isEnabled = newValue

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
        when (it.key) {
            "export_key" -> {
                try {
                    if (driveServiceHelper == null) {
                        googleAuth()
                    }
                    BackupUtil.exportDataToDrive(appContext, driveServiceHelper!!)
                } catch (e: UserRecoverableAuthIOException) {
                    startActivityForResult(e.intent, AppResultCodes.userRecoverableAuth)
                }
            }
            getString(R.string.backup_drive_import) -> {
                val progressDialog = ApplicationUtil.createProgressDialog(
                    appContext,
                    R.string.dialog_backup_progress_open_file_list
                )
                progressDialog.show()
                try {
                    if (driveServiceHelper == null) {
                        googleAuth()
                    }
                    var folderId = BackupUtil.driveRootFolderId
                    driveServiceHelper!!.getFolderIdByName("car-logbook-backup")
                        .addOnCompleteListener { searchResult ->
                            folderId = searchResult.result ?: folderId
                        }.continueWithTask {
                            driveServiceHelper!!.getAllFilesInFolder(folderId)
                                .addOnCompleteListener { fileList ->
                                    val files = fileList.result ?: ArrayList()
                                    val driveImportDialog = DriveImportDialog(appContext, driveServiceHelper!!, files)
                                    val layoutParams = WindowManager.LayoutParams()
                                    layoutParams.copyFrom(driveImportDialog.window!!.attributes)
                                    layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
                                    progressDialog.dismiss()
                                    driveImportDialog.show()
                                    driveImportDialog.window!!.attributes = layoutParams
                                }
                        }
                } catch (e: UserRecoverableAuthIOException) {
                    progressDialog.dismiss()
                    startActivityForResult(e.intent, AppResultCodes.userRecoverableAuth)
                }
            }
            getString(R.string.backup_export_key) -> {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), AppResultCodes.backupOpenDocumentTree)
            }
            getString(R.string.backup_file_import) -> {
                val openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                openDocumentIntent.addCategory(Intent.CATEGORY_OPENABLE)
                openDocumentIntent.type = "application/*"
                startActivityForResult(openDocumentIntent, AppResultCodes.backupOpenDocument)
            }
        }

        true
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
                AppResultCodes.getAccountsPermission
            )
        }
    }

    private fun googleAuth() {
        checkPermissions()

        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(appContext)
        if (googleSignInAccount == null) {
            startActivityForResult(getGoogleSignInClient().signInIntent,
                AppResultCodes.googleSignIn
            )
        } else {
            driveServiceHelper = initDriveServiceHelper(googleSignInAccount.account)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            AppResultCodes.googleSignIn -> {
                if (resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    task.addOnSuccessListener {
                        driveServiceHelper = initDriveServiceHelper(it.account)
                    }
                }
            }
            AppResultCodes.backupOpenDocumentTree -> {
                if (resultCode == Activity.RESULT_OK) {
                    val progressDialog = ApplicationUtil.createProgressDialog(
                        appContext,
                        R.string.dialog_backup_progress_export_text
                    )
                    progressDialog.show()
                    BackupUtil.exportDataToFile(data!!.data!!, appContext, progressDialog)
                }
            }
            AppResultCodes.backupOpenDocument -> {
                if (resultCode == Activity.RESULT_OK) {
                    val progressDialog = ApplicationUtil.createProgressDialog(
                        appContext,
                        R.string.dialog_backup_progress_import_text
                    )
                    progressDialog.show()
                    BackupUtil.importDataFromFile(data!!.data!!, appContext, progressDialog)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            AppResultCodes.internetPermission -> {
                if (grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(appContext, "Access to the internet is required", Toast.LENGTH_LONG).show()
                    disableSyncPreferenceItems()
                }
            }
            AppResultCodes.getAccountsPermission -> {
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
            AndroidHttp.newCompatibleTransport(), GsonFactory(),
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