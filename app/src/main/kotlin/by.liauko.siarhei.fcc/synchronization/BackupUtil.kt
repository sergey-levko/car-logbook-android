package by.liauko.siarhei.fcc.synchronization

import android.content.Context
import android.os.Environment
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.fcc.database.entity.LogEntity
import by.liauko.siarhei.fcc.repository.DeleteAllAsyncTask
import by.liauko.siarhei.fcc.repository.InsertAllAsyncTask
import by.liauko.siarhei.fcc.repository.SelectAsyncTask
import by.liauko.siarhei.fcc.util.DataType
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object BackupUtil {

    fun exportData(context: Context) {
        val exportDir = File(Environment.getExternalStorageDirectory(), "car-logdir")
        if (!exportDir.exists()) {
            exportDir.mkdir()
        }
        val backupFile = File(exportDir, "car-logbook" /*+ SimpleDateFormat.getDateInstance().format(Date())*/ + ".clbdata")
        backupFile.createNewFile()
        val logEntities = SelectAsyncTask(DataType.LOG, CarLogDatabase.invoke(context)).execute().get() as List<LogEntity>
        val fuelConsumptionEntities = SelectAsyncTask(DataType.FUEL, CarLogDatabase.invoke(context)).execute().get() as List<FuelConsumptionEntity>
        val backUpData = BackUpEntity(logEntities, fuelConsumptionEntities)
        val output = BufferedWriter(FileWriter(backupFile))
        output.write(Gson().toJson(backUpData))
        output.close()
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