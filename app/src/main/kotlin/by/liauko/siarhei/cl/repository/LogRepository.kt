package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.database.CarLogDatabase
import by.liauko.siarhei.cl.database.entity.AppEntity
import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.entity.LogData
import by.liauko.siarhei.cl.util.ApplicationUtil.periodCalendar
import by.liauko.siarhei.cl.util.DataType

class LogRepository(context: Context) : Repository {

    private val database = CarLogDatabase(context)
    private val type = DataType.LOG

    override fun selectAll() =
        SelectAsyncTask(type, database).execute().get().map { convertToData(it as LogEntity) }

    override fun selectAllByPeriod(): List<LogData> {
        val timeBounds = prepareDateRange(periodCalendar)
        return SelectByDateAsyncTask(type, database)
            .execute(timeBounds.first, timeBounds.second)
            .get()
            .map { convertToData(it as LogEntity) }
    }

    override fun insert(entity: AppEntity): Long =
        InsertAsyncTask(type, database).execute(entity as LogEntity).get()

    override fun update(data: AppData) {
        UpdateAsyncTask(type, database).execute(convertToEntity(data as LogData))
    }

    override fun delete(data: AppData) {
        DeleteAsyncTask(type, database).execute(convertToEntity(data as LogData))
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
}