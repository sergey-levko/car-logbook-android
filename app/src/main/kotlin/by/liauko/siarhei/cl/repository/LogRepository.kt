package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.database.CarLogDatabase
import by.liauko.siarhei.cl.database.entity.AppEntity
import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.entity.LogData
import by.liauko.siarhei.cl.util.ApplicationUtil.periodCalendar
import by.liauko.siarhei.cl.util.DataType

class LogRepository(context: Context) : DataRepository {

    private val database = CarLogDatabase(context)
    private val type = DataType.LOG

    override fun selectAll() =
        SelectAsyncTask(type, database).execute().get().map { convertToData(it as LogEntity) }

    override fun selectAllByProfileId(profileId: Long) =
        SelectByProfileIdAsyncTask(type, database).execute(profileId).get().map { convertToData(it as LogEntity) }

    override fun selectAllByProfileIdAndPeriod(profileId: Long): List<LogData> {
        val timeBounds = prepareDateRange(periodCalendar)
        return SelectByProfileIdAndDateAsyncTask(type, database)
            .execute(profileId, timeBounds.first, timeBounds.second)
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

    override fun deleteAllByProfileId(profileId: Long) {
        DeleteAllByProfileId(type, database).execute(profileId)
    }

    private fun convertToEntity(data: LogData) =
        LogEntity(
            data.id,
            data.title,
            data.text,
            data.mileage,
            data.time,
            null
        )

    private fun convertToData(entity: LogEntity) =
        LogData(
            entity.id!!,
            entity.time,
            entity.title,
            entity.text,
            entity.mileage
        )
}
