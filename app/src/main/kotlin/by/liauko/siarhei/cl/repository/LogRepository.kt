package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.entity.LogData
import by.liauko.siarhei.cl.util.ApplicationUtil.periodCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext

class LogRepository(context: Context) :
    DataRepository,
    CoroutineScope by MainScope() {

    private val dao = CarLogbookDatabase(context).logDao()

    override suspend fun selectAll() =
        withContext(Dispatchers.Default) {
            dao.findAll().map { convertToData(it) }
        }

    override suspend fun selectAllByProfileId(profileId: Long) =
        withContext(Dispatchers.Default) {
            dao.findAllByProfileId(profileId).map { convertToData(it) }
        }

    override suspend fun selectAllByProfileIdAndPeriod(profileId: Long): List<LogData> {
        val timeBounds = prepareDateRange(periodCalendar)
        return withContext(Dispatchers.Default) {
            dao.findAllByProfileIdAndDate(
                profileId,
                timeBounds.first,
                timeBounds.second
            ).map { convertToData(it) }
        }
    }

    override suspend fun insert(data: AppData) =
        withContext(Dispatchers.Default) {
            dao.insert(convertToEntity(data as LogData))
        }

    override suspend fun update(data: AppData) =
        withContext(Dispatchers.Default) {
            dao.update(convertToEntity(data as LogData))
        }

    override suspend fun delete(data: AppData) =
        withContext(Dispatchers.Default) {
            dao.delete(convertToEntity(data as LogData))
        }

    override suspend fun deleteAllByProfileId(profileId: Long) =
        withContext(Dispatchers.Default) {
            dao.deleteAllByProfileId(profileId)
        }

    private fun convertToEntity(data: LogData) =
        LogEntity(
            data.id,
            data.title,
            data.text,
            data.mileage,
            data.time,
            data.profileId
        )

    private fun convertToData(entity: LogEntity) =
        LogData(
            entity.id!!,
            entity.time,
            entity.title,
            entity.text,
            entity.mileage,
            entity.profileId
        )
}
