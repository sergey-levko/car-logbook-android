package by.liauko.siarhei.fcc.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.activity.dialog.ProgressDialog
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.fcc.database.entity.LogEntity
import by.liauko.siarhei.fcc.drive.DriveMimeTypes
import by.liauko.siarhei.fcc.drive.DriveServiceHelper
import by.liauko.siarhei.fcc.repository.DeleteAllAsyncTask
import by.liauko.siarhei.fcc.repository.InsertAllAsyncTask
import by.liauko.siarhei.fcc.repository.SelectAsyncTask
import by.liauko.siarhei.fcc.util.ApplicationUtil
import by.liauko.siarhei.fcc.util.DataType
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupUtil {

    const val driveRootFolderId = "root"

    fun exportDataToDrive(context: Context, driveServiceHelper: DriveServiceHelper) {
        val progressDialog = ApplicationUtil.createProgressDialog(
            context,
            R.string.dialog_backup_progress_export_text
        )
        progressDialog.show()

        val backupData = prepareBackupData(CarLogDatabase.invoke(context))

        var folderId = driveRootFolderId
        driveServiceHelper.createFolderIfNotExist("car-logbook-backup").addOnCompleteListener {
            folderId = it.result ?: driveRootFolderId
        }.continueWithTask {
            driveServiceHelper.createFile(
                folderId,
                "car-logbook-${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.clbdata",
                Gson().toJson(backupData)
            ).addOnCompleteListener {
                progressDialog.dismiss()
                ApplicationUtil.createAlertDialog(
                    context,
                    R.string.dialog_backup_alert_title_success,
                    R.string.dialog_backup_alert_export_success
                ).show()
            }.addOnFailureListener {
                progressDialog.dismiss()
                ApplicationUtil.createAlertDialog(
                    context,
                    R.string.dialog_backup_alert_title_fail,
                    R.string.dialog_backup_alert_export_fail
                ).show()
            }
        }
    }

    fun importDataFromDrive(fileId: String, context: Context, driveServiceHelper: DriveServiceHelper) {
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
                    ApplicationUtil.createAlertDialog(
                        context,
                        R.string.dialog_backup_alert_title_fail,
                        R.string.dialog_backup_alert_import_fail
                    ).show()
                }
            }
            .addOnFailureListener {
                ApplicationUtil.createAlertDialog(
                    context,
                    R.string.dialog_backup_alert_title_fail,
                    R.string.dialog_backup_alert_import_fail
                ).show()
            }
    }

    fun exportDataToFile(directoryUri: Uri, context: Context, progressDialog: ProgressDialog) {
        val logEntities = SelectAsyncTask(DataType.LOG, CarLogDatabase.invoke(context)).execute().get() as List<LogEntity>
        val fuelConsumptionEntities = SelectAsyncTask(DataType.FUEL, CarLogDatabase.invoke(context)).execute().get() as List<FuelConsumptionEntity>
        val backUpData = BackupEntity(logEntities, fuelConsumptionEntities)

        val file = DocumentFile.fromTreeUri(context, directoryUri)!!.createFile(
            DriveMimeTypes.TYPE_JSON_FILE.mimeType,
            "car-logbook-${SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(Date())}.clbdata"
        )!!
        context.contentResolver.openOutputStream(file.uri)!!.use {
            it.write(Gson().toJson(backUpData).toByteArray())
            it.flush()
        }
        progressDialog.dismiss()
        ApplicationUtil.createAlertDialog(
            context,
            R.string.dialog_backup_alert_title_success,
            R.string.dialog_backup_alert_export_success
        ).show()
    }

    fun importDataFromFile(fileUri: Uri, context: Context, progressDialog: ProgressDialog) {
        context.contentResolver.openInputStream(fileUri)!!.bufferedReader().use {
            restoreData(
                CarLogDatabase.invoke(context),
                Gson().fromJson<BackupEntity>(it.readLine(), BackupEntity::class.java)
            )
        }
        progressDialog.dismiss()
        ApplicationUtil.createAlertDialog(
            context,
            R.string.dialog_backup_alert_title_success,
            R.string.dialog_backup_alert_import_success
        ).show()
    }
    
    private fun prepareBackupData(database: CarLogDatabase): BackupEntity {
        val logEntities = SelectAsyncTask(DataType.LOG, database).execute().get() as List<LogEntity>
        val fuelConsumptionEntities = SelectAsyncTask(DataType.FUEL, database).execute().get() as List<FuelConsumptionEntity>
        return BackupEntity(logEntities, fuelConsumptionEntities)
    }
    
    private fun restoreData(database: CarLogDatabase, backupData: BackupEntity) {
        DeleteAllAsyncTask(DataType.LOG, database).execute()
        InsertAllAsyncTask(DataType.LOG,  database).execute(backupData.logEntities)
        DeleteAllAsyncTask(DataType.FUEL, database).execute()
        InsertAllAsyncTask(DataType.FUEL, database).execute(backupData.fuelConsumptionEntities)
    }
}