package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.entity.LogData
import by.liauko.siarhei.cl.repository.converter.LogDataConverter
import by.liauko.siarhei.cl.util.ApplicationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext

class LogRepository(context: Context) :
    DataRepository<LogData>,
    CoroutineScope by MainScope() {

    private val dao = CarLogbookDatabase(context).logDao()

    override suspend fun selectAll() =
        withContext(Dispatchers.Default) {
            dao.findAll().map { LogDataConverter.convertToData(it) }
        }

    override suspend fun selectAllByProfileId(profileId: Long) =
        withContext(Dispatchers.Default) {
            dao.findAllByProfileId(profileId).map { LogDataConverter.convertToData(it) }
        }

    override suspend fun selectAllByProfileIdAndPeriod(profileId: Long): List<LogData> {
        val timeBounds = ApplicationUtil.prepareDateRange()
        return withContext(Dispatchers.Default) {
            dao.findAllByProfileIdAndDate(
                profileId,
                timeBounds.first,
                timeBounds.second
            ).map { LogDataConverter.convertToData(it) }
        }
    }

    override suspend fun insert(data: LogData) =
        withContext(Dispatchers.Default) {
            dao.insert(LogDataConverter.convertToEntity(data))
        }

    suspend fun insertAll(data: List<LogEntity>) =
        withContext(Dispatchers.Default) {
            dao.insertAll(data)
        }

    override suspend fun update(data: LogData) =
        withContext(Dispatchers.Default) {
            dao.update(LogDataConverter.convertToEntity(data))
        }

    override suspend fun delete(data: LogData) =
        withContext(Dispatchers.Default) {
            dao.delete(LogDataConverter.convertToEntity(data))
        }

    override suspend fun deleteAll() =
        withContext(Dispatchers.Default) {
            dao.deleteAll()
        }

    override suspend fun deleteAllByProfileId(profileId: Long) =
        withContext(Dispatchers.Default) {
            dao.deleteAllByProfileId(profileId)
        }
}
