package by.liauko.siarhei.cl.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.dialog.ProgressDialog
import by.liauko.siarhei.cl.database.CarLogDatabase
import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.drive.DriveMimeTypes
import by.liauko.siarhei.cl.drive.DriveServiceHelper
import by.liauko.siarhei.cl.repository.DeleteAllAsyncTask
import by.liauko.siarhei.cl.repository.DeleteAllCarProfilesAsyncTask
import by.liauko.siarhei.cl.repository.InsertAllAsyncTask
import by.liauko.siarhei.cl.repository.InsertAllCarProfileAsyncTask
import by.liauko.siarhei.cl.repository.SelectAllCarProfileAsyncTask
import by.liauko.siarhei.cl.repository.SelectAsyncTask
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.ApplicationUtil.profileId
import by.liauko.siarhei.cl.util.ApplicationUtil.profileName
import by.liauko.siarhei.cl.util.DataType
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupService {

    private const val MAX_FILE_COUNT = 16
    private const val EMPTY_JSON_OBJECT = "{}"

    fun exportToDrive(context: Context, driveServiceHelper: DriveServiceHelper) {
        val backupData = prepareBackupData(CarLogDatabase.invoke(context))

        val folderId = driveServiceHelper.createFolderIfNotExist("car-logbook-backup")
        val files = driveServiceHelper.getAllFilesInFolder(folderId).sortedBy { item -> item.first }
        if (files.size >= MAX_FILE_COUNT) {
            for (item in files.subList(0, files.size - MAX_FILE_COUNT + 1)) {
                driveServiceHelper.deleteFile(item.second)
            }
        }
        driveServiceHelper.createFile(
            folderId,
            "car-logbook-${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.clbdata",
            Gson().toJson(backupData)
        )
    }

    fun importFromDrive(fileId: String, context: Context, driveServiceHelper: DriveServiceHelper) {
        val progressDialog = ApplicationUtil.createProgressDialog(
            context,
            R.string.dialog_backup_progress_import_text
        )
        progressDialog.show()

        driveServiceHelper.readBackupFile(fileId)
            .addOnCompleteListener {
                val data = it.result
                if (data != null) {
                    restoreData(CarLogDatabase.invoke(context), data)
                    progressDialog.dismiss()
                    ApplicationUtil.createAlertDialog(
                        context,
                        R.string.dialog_backup_alert_title_success,
                        R.string.dialog_backup_alert_import_success
                    ).show()
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
    fun exportToFile(directoryUri: Uri, context: Context, progressDialog: ProgressDialog) {
        val database = CarLogDatabase.invoke(context)
        val logEntities = SelectAsyncTask(DataType.LOG, database).execute().get() as List<LogEntity>
        val fuelConsumptionEntities = SelectAsyncTask(DataType.FUEL, database).execute().get() as List<FuelConsumptionEntity>
        val carProfileEntities = SelectAllCarProfileAsyncTask(database).execute().get()
        val backUpData = BackupEntity(logEntities, fuelConsumptionEntities, carProfileEntities)

        val file = DocumentFile.fromTreeUri(context, directoryUri)?.createFile(
            DriveMimeTypes.TYPE_JSON_FILE,
            "car-logbook-${SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(Date())}.clbdata"
        )
        if (file?.uri != null) {
            context.contentResolver.openOutputStream(file.uri)?.use {
                it.write(Gson().toJson(backUpData).toByteArray())
                it.flush()
            }
        }
        progressDialog.dismiss()
        ApplicationUtil.createAlertDialog(
            context,
            R.string.dialog_backup_alert_title_success,
            R.string.dialog_backup_alert_export_success
        ).show()
    }

    fun importFromFile(fileUri: Uri, context: Context, progressDialog: ProgressDialog) {
        context.contentResolver.openInputStream(fileUri)?.bufferedReader().use {
            restoreData(
                CarLogDatabase.invoke(context),
                Gson().fromJson<BackupEntity>(it?.readLine() ?: EMPTY_JSON_OBJECT, BackupEntity::class.java)
            )
        }
        progressDialog.dismiss()
        ApplicationUtil.createAlertDialog(
            context,
            R.string.dialog_backup_alert_title_success,
            R.string.dialog_backup_alert_import_success
        ).show()
    }

    fun eraseAllData(context: Context) {
        val database = CarLogDatabase.invoke(context)
        DeleteAllAsyncTask(DataType.LOG, database).execute()
        DeleteAllAsyncTask(DataType.FUEL, database).execute()
        DeleteAllCarProfilesAsyncTask(database).execute()
        profileId = -1L
        profileName = context.getString(R.string.app_name)
    }

    @Suppress("UNCHECKED_CAST")
    private fun prepareBackupData(database: CarLogDatabase): BackupEntity {
        val logEntities = SelectAsyncTask(DataType.LOG, database).execute().get() as List<LogEntity>
        val fuelConsumptionEntities = SelectAsyncTask(DataType.FUEL, database).execute().get() as List<FuelConsumptionEntity>
        val carProfileEntities = SelectAllCarProfileAsyncTask(database).execute().get()
        return BackupEntity(logEntities, fuelConsumptionEntities, carProfileEntities)
    }
    
    private fun restoreData(database: CarLogDatabase, backupData: BackupEntity) {
        DeleteAllAsyncTask(DataType.LOG, database).execute()
        InsertAllAsyncTask(DataType.LOG,  database).execute(backupData.logEntities)
        DeleteAllAsyncTask(DataType.FUEL, database).execute()
        InsertAllAsyncTask(DataType.FUEL, database).execute(backupData.fuelConsumptionEntities)
        DeleteAllCarProfilesAsyncTask(database).execute()
        InsertAllCarProfileAsyncTask(database).execute(backupData.carProfileEntities)
        profileId = backupData.carProfileEntities.first().id!!
        profileName = backupData.carProfileEntities.first().name
    }
}