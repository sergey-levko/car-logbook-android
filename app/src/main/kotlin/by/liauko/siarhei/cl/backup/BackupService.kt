package by.liauko.siarhei.cl.backup

import android.accounts.Account
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.dialog.DriveImportDialog
import by.liauko.siarhei.cl.backup.adapter.BackupAdapter
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.CarProfileEntity
import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.drive.DRIVE_ROOT_FOLDER_ID
import by.liauko.siarhei.cl.drive.DriveServiceHelper
import by.liauko.siarhei.cl.repository.DeleteAllAsyncTask
import by.liauko.siarhei.cl.repository.DeleteAllCarProfilesAsyncTask
import by.liauko.siarhei.cl.repository.InsertAllAsyncTask
import by.liauko.siarhei.cl.repository.InsertAllCarProfileAsyncTask
import by.liauko.siarhei.cl.repository.InsertCarProfileAsyncTask
import by.liauko.siarhei.cl.repository.SelectAllCarProfileAsyncTask
import by.liauko.siarhei.cl.repository.SelectAsyncTask
import by.liauko.siarhei.cl.util.AppResultCodes.BACKUP_OPEN_DOCUMENT
import by.liauko.siarhei.cl.util.AppResultCodes.GOOGLE_SIGN_IN
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.ApplicationUtil.profileId
import by.liauko.siarhei.cl.util.ApplicationUtil.profileName
import by.liauko.siarhei.cl.util.DataType
import by.liauko.siarhei.cl.util.ExportToFileAsyncTask
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
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object BackupService {

    private const val MAX_FILE_COUNT = 16

    const val WORK_TAG = "car-logbook-backup-work"

    var driveServiceHelper: DriveServiceHelper? = null
    var repeatInterval = 0L
    var backupTask = BackupTask.IMPORT

    fun googleAuth(adapter: BackupAdapter) {
        val context = adapter.getContextForAuth()

        PermissionService.checkPermissions(adapter.getActivityForPermissions())

        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleSignInAccount == null) {
            adapter.startActivityForResult(
                getGoogleSignInClient(context).signInIntent,
                GOOGLE_SIGN_IN
            )
        } else {
            executeBackupTask(
                adapter.getContext(),
                googleSignInAccount.account,
                adapter.getActivity()
            )
        }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    Scope(Scopes.PROFILE),
                    Scope("https://www.googleapis.com/auth/drive")
                )
                .build()
        return GoogleSignIn.getClient(context, googleSignInOptions)
    }

    fun googleSignInResult(context: Context, data: Intent?, activity: Activity?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        task.addOnSuccessListener {
            executeBackupTask(context, it.account, activity)
        }
    }

    fun startBackupWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest =
            PeriodicWorkRequestBuilder<CoroutineBackupWorker>(repeatInterval, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
    }

    fun importDataFromDrive(context: Context, activity: Activity?) {
        val progressDialog = ApplicationUtil.createProgressDialog(
            context,
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
                        val driveImportDialog =
                            DriveImportDialog(context, driveServiceHelper!!, files, activity)
                        progressDialog.dismiss()
                        driveImportDialog.show()
                    }
            }
    }

    fun exportToDrive(context: Context, driveServiceHelper: DriveServiceHelper) {
        val backupData = prepareBackupData(CarLogbookDatabase.invoke(context))

        val folderId = driveServiceHelper.createFolderIfNotExist("car-logbook-backup")
        val files = driveServiceHelper.getAllFilesInFolder(folderId).sortedBy { item -> item.first }
        if (files.size >= MAX_FILE_COUNT) {
            for (item in files.subList(0, files.size - MAX_FILE_COUNT + 1)) {
                driveServiceHelper.deleteFile(item.second)
            }
        }
        driveServiceHelper.createFile(
            folderId,
            "car-logbook-${SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())}.clbdata",
            Gson().toJson(backupData)
        )
    }

    fun importFromDrive(
        fileId: String,
        context: Context,
        driveServiceHelper: DriveServiceHelper,
        activity: Activity?
    ) {
        val progressDialog = ApplicationUtil.createProgressDialog(
            context,
            R.string.dialog_backup_progress_import_text
        )
        progressDialog.show()

        driveServiceHelper.readBackupFile(fileId)
            .addOnCompleteListener {
                val data = it.result
                if (data != null) {
                    restoreData(CarLogbookDatabase.invoke(context), data)
                    saveProfileValues(context)
                    progressDialog.dismiss()
                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.dialog_backup_alert_title_success)
                        .setMessage(R.string.dialog_backup_alert_import_success)
                        .setPositiveButton(
                            context.getString(
                                R.string.dialog_backup_alert_ok_button
                            )
                        ) { dialog, _ ->
                            activity?.setResult(RESULT_OK)
                            activity?.finish()
                            dialog.dismiss()
                        }
                        .create().show()
                } else {
                    progressDialog.dismiss()
                    ApplicationUtil.createAlertDialog(
                        context,
                        R.string.dialog_backup_alert_title_fail,
                        R.string.dialog_backup_alert_import_fail
                    ).show()
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                ApplicationUtil.createAlertDialog(
                    context,
                    R.string.dialog_backup_alert_title_fail,
                    R.string.dialog_backup_alert_import_fail
                ).show()
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun exportToFile(directoryUri: Uri, context: Context) {
        val progressDialog = ApplicationUtil.createProgressDialog(
            context,
            R.string.dialog_backup_progress_export_text
        )
        progressDialog.show()

        val database = CarLogbookDatabase.invoke(context)
        val logEntities = SelectAsyncTask(DataType.LOG, database).execute().get() as List<LogEntity>
        val fuelConsumptionEntities =
            SelectAsyncTask(DataType.FUEL, database).execute().get() as List<FuelConsumptionEntity>
        val carProfileEntities = SelectAllCarProfileAsyncTask(database).execute().get()
        val backUpData = BackupEntity(logEntities, fuelConsumptionEntities, carProfileEntities)

        ExportToFileAsyncTask(
            directoryUri,
            context,
            backUpData,
            progressDialog
        ).execute()
    }

    fun openDocument(adapter: BackupAdapter) {
        val openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        openDocumentIntent.addCategory(Intent.CATEGORY_OPENABLE)
        openDocumentIntent.type = "application/*"
        adapter.startActivityForResult(openDocumentIntent, BACKUP_OPEN_DOCUMENT)
    }

    fun eraseAllData(context: Context) {
        val database = CarLogbookDatabase.invoke(context)
        DeleteAllAsyncTask(DataType.LOG, database).execute()
        DeleteAllAsyncTask(DataType.FUEL, database).execute()
        DeleteAllCarProfilesAsyncTask(database).execute()
        profileId = -1L
        profileName = context.getString(R.string.app_name)
        saveProfileValues(context)
    }

    private fun executeBackupTask(context: Context, account: Account?, activity: Activity?) {
        driveServiceHelper = initDriveServiceHelper(context, account)
        when (backupTask) {
            BackupTask.IMPORT -> importDataFromDrive(context, activity)
            BackupTask.EXPORT -> exportDataToDrive(context)
        }
    }

    private fun initDriveServiceHelper(context: Context, account: Account?): DriveServiceHelper {
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE))
        credential.selectedAccount = account
        val googleDriveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName(context.getString(R.string.app_name)).build()

        return DriveServiceHelper(googleDriveService)
    }

    private fun exportDataToDrive(context: Context) {
        if (repeatInterval != 0L) {
            startBackupWorker(context)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun prepareBackupData(database: CarLogbookDatabase): BackupEntity {
        val logEntities = SelectAsyncTask(DataType.LOG, database).execute().get() as List<LogEntity>
        val fuelConsumptionEntities =
            SelectAsyncTask(DataType.FUEL, database).execute().get() as List<FuelConsumptionEntity>
        val carProfileEntities = SelectAllCarProfileAsyncTask(database).execute().get()
        return BackupEntity(logEntities, fuelConsumptionEntities, carProfileEntities)
    }

    fun restoreData(database: CarLogbookDatabase, backupData: BackupEntity) {
        DeleteAllCarProfilesAsyncTask(database).execute()
        if (backupData.carProfileEntities != null) {
            InsertAllCarProfileAsyncTask(database).execute(backupData.carProfileEntities)
            profileId = backupData.carProfileEntities.first().id!!
            profileName = backupData.carProfileEntities.first().name
        } else { // Case when user try to import file with data
            // which was exported before car profile functionality was implemented
            val carProfileEntity =
                CarProfileEntity(null, "Default Car Profile", "SEDAN", "GASOLINE", 1.0)
            val id = InsertCarProfileAsyncTask(database)
                .execute(carProfileEntity)
                .get()
            if (id != null) {
                profileId = id
                profileName = carProfileEntity.name
                backupData.logEntities.forEach { it.profileId = profileId }
                backupData.fuelConsumptionEntities.forEach { it.profileId = profileId }
            }
        }

        DeleteAllAsyncTask(DataType.LOG, database).execute()
        InsertAllAsyncTask(DataType.LOG, database).execute(backupData.logEntities)
        DeleteAllAsyncTask(DataType.FUEL, database).execute()
        InsertAllAsyncTask(DataType.FUEL, database).execute(backupData.fuelConsumptionEntities)
    }

    fun saveProfileValues(context: Context) {
        context.getSharedPreferences(
            context.getString(R.string.shared_preferences_name),
            Context.MODE_PRIVATE
        )
            .edit()
            .putLong(context.getString(R.string.car_profile_id_key), profileId)
            .putString(context.getString(R.string.car_profile_name_key), profileName)
            .apply()
    }
}

enum class BackupTask {
    IMPORT, EXPORT
}
