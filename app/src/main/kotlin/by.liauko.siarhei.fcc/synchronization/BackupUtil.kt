package by.liauko.siarhei.fcc.synchronization

import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.fcc.database.entity.LogEntity
import by.liauko.siarhei.fcc.drive.DriveServiceHelper
import by.liauko.siarhei.fcc.repository.DeleteAllAsyncTask
import by.liauko.siarhei.fcc.repository.InsertAllAsyncTask
import by.liauko.siarhei.fcc.repository.SelectAsyncTask
import by.liauko.siarhei.fcc.util.DataType
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupUtil {

    private const val driveRootFolderId = "root"

    fun exportData(context: Context, driveServiceHelper: DriveServiceHelper) {
        val progressDialog = AlertDialog.Builder(context)
            .setView(R.layout.dialog_progress)
            .create()
        progressDialog.setTitle(R.string.dialog_progress_title)
        progressDialog.show()

        val logEntities = SelectAsyncTask(DataType.LOG, CarLogDatabase.invoke(context)).execute().get() as List<LogEntity>
        val fuelConsumptionEntities = SelectAsyncTask(DataType.FUEL, CarLogDatabase.invoke(context)).execute().get() as List<FuelConsumptionEntity>
        val backUpData = BackUpEntity(logEntities, fuelConsumptionEntities)

        var folderId = driveRootFolderId
        driveServiceHelper.createFolderIfNotExist("car-logbook-backup").addOnCompleteListener {
            folderId = it.result ?: driveRootFolderId
        }.continueWithTask {
            driveServiceHelper.createFile(
                folderId,
                "car-logbook-${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.clbdata",
                Gson().toJson(backUpData)
            ).addOnCompleteListener {
                progressDialog.hide()
            }
        }
    }

    fun importData(context: Context) {
        val backupFile = File(Environment.getExternalStorageDirectory(), "car-logdir/car-logbook.clbdata")
        val inputStream = BufferedReader(FileReader(backupFile))
        val backupData = Gson().fromJson<BackUpEntity>(inputStream.readLine(), BackUpEntity::class.java)
        inputStream.close()

        DeleteAllAsyncTask(DataType.LOG, CarLogDatabase.invoke(context)).execute()
        InsertAllAsyncTask(DataType.LOG,  CarLogDatabase.invoke(context)).execute(backupData.logEntities)
        DeleteAllAsyncTask(DataType.FUEL, CarLogDatabase.invoke(context)).execute()
        InsertAllAsyncTask(DataType.FUEL, CarLogDatabase.invoke(context)).execute(backupData.fuelConsumptionEntities)
    }
}