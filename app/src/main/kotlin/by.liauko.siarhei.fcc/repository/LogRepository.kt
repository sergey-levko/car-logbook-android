package by.liauko.siarhei.fcc.repository

import android.content.Context
import android.os.AsyncTask
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.database.dao.LogDao
import by.liauko.siarhei.fcc.database.entity.AppEntity
import by.liauko.siarhei.fcc.database.entity.LogEntity
import by.liauko.siarhei.fcc.entity.AppData
import by.liauko.siarhei.fcc.entity.LogData

class LogRepository(context: Context): Repository {
    private val logDao: LogDao

    init {
        val database = CarLogDatabase(context)
        logDao = database.logDao()
    }

    override fun selectAll(): List<LogData> {
        val items = mutableListOf<LogData>() as ArrayList
        val entities = SelectAsyncTask(logDao).execute().get()
        for (entity in entities) {
            items.add(convertToData(entity))
        }

        return items
    }

    override fun insert(entity: AppEntity) =
        InsertAsyncTask(logDao).execute(entity as LogEntity).get()

    override fun update(data: AppData) {
        UpdateAsyncTask(logDao).execute(convertToEntity(data as LogData))
    }

    override fun delete(data: AppData) {
        DeleteAsyncTask(logDao).execute(convertToEntity(data as LogData))
    }

    private fun convertToEntity(logData: LogData) =
        LogEntity(
            logData.id,
            logData.title,
            logData.text,
            logData.mileage,
            logData.time
        )

    private fun convertToData(logEntity: LogEntity) =
        LogData(
            logEntity.id!!,
            logEntity.time,
            logEntity.title,
            logEntity.text,
            logEntity.mileage
        )

    private class SelectAsyncTask(private val logDao: LogDao): AsyncTask<Unit, Unit, List<LogEntity>>() {
        override fun doInBackground(vararg params: Unit?): List<LogEntity> = logDao.findAll()
    }

    private class InsertAsyncTask(private val logDao: LogDao): AsyncTask<LogEntity, Unit, Long>() {
        override fun doInBackground(vararg params: LogEntity?) = logDao.insert(params[0]!!)
    }

    private class UpdateAsyncTask(private val logDao: LogDao): AsyncTask<LogEntity, Unit, Unit>() {
        override fun doInBackground(vararg params: LogEntity?) {
            logDao.update(params[0]!!)
        }
    }

    private class DeleteAsyncTask(private val logDao: LogDao): AsyncTask<LogEntity, Unit, Unit>() {
        override fun doInBackground(vararg params: LogEntity?) {
            logDao.delete(params[0]!!)
        }
    }
}
