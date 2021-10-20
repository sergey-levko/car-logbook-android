package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.entity.FuelConsumptionData
import by.liauko.siarhei.cl.repository.converter.FuelConsumptionConverter
import by.liauko.siarhei.cl.util.ApplicationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext

class FuelConsumptionRepository(context: Context) :
    DataRepository<FuelConsumptionData>,
    CoroutineScope by MainScope() {

    private val dao = CarLogbookDatabase(context).fuelConsumptionDao()

    override suspend fun selectAll() =
        withContext(Dispatchers.Default) {
            dao.findAll().map { FuelConsumptionConverter.convertToData(it) }
        }

    override suspend fun selectAllByProfileId(profileId: Long) =
        withContext(Dispatchers.Default) {
            dao.findAllByProfileId(profileId).map { FuelConsumptionConverter.convertToData(it) }
        }

    override suspend fun selectAllByProfileIdAndPeriod(profileId: Long): List<FuelConsumptionData> {
        val timeBounds = ApplicationUtil.prepareDateRange()
        return withContext(Dispatchers.Default) {
            dao.findAllByProfileIdAndDate(
                profileId,
                timeBounds.first,
                timeBounds.second
            ).map { FuelConsumptionConverter.convertToData(it) }
        }
    }

    override suspend fun insert(data: FuelConsumptionData) =
        withContext(Dispatchers.Default) {
            dao.insert(FuelConsumptionConverter.convertToEntity(data))
        }

    suspend fun insertAll(data: List<FuelConsumptionEntity>) =
        withContext(Dispatchers.Default) {
            dao.insertAll(data)
        }

    override suspend fun update(data: FuelConsumptionData) =
        withContext(Dispatchers.Default) {
            dao.update(FuelConsumptionConverter.convertToEntity(data))
        }

    override suspend fun delete(data: FuelConsumptionData) =
        withContext(Dispatchers.Default) {
            dao.delete(FuelConsumptionConverter.convertToEntity(data))
        }

    override suspend fun deleteAll() =
        withContext(Dispatchers.Default) {
            dao.deleteAll()
        }

    override suspend fun deleteAllByProfileId(profileId: Long) =
        withContext(Dispatchers.Default) {
            dao.deleteAllByProfileId(profileId)
        }

    suspend fun selectLastMileage(profileId: Long) =
        withContext(Dispatchers.Default) {
            dao.findLastMileage(profileId)
        }
}
