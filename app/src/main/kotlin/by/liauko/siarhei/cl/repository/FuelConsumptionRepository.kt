package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.entity.FuelConsumptionData
import by.liauko.siarhei.cl.util.ApplicationUtil.periodCalendar
import by.liauko.siarhei.cl.util.DataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext

class FuelConsumptionRepository(context: Context) :
    DataRepository,
    CoroutineScope by MainScope() {

    private val dao = CarLogbookDatabase(context).fuelConsumptionDao()
    private val type = DataType.FUEL

    override suspend fun selectAll() =
        withContext(Dispatchers.Default) {
            dao.findAll().map { convertToData(it) }
        }

    override suspend fun selectAllByProfileId(profileId: Long) =
        withContext(Dispatchers.Default) {
            dao.findAllByProfileId(profileId).map { convertToData(it) }
        }

    override suspend fun selectAllByProfileIdAndPeriod(profileId: Long): List<FuelConsumptionData> {
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
            dao.insert(convertToEntity(data as FuelConsumptionData))
        }

    override suspend fun update(data: AppData) =
        withContext(Dispatchers.Default) {
            dao.update(convertToEntity(data as FuelConsumptionData))
        }

    override suspend fun delete(data: AppData) =
        withContext(Dispatchers.Default) {
            dao.delete(convertToEntity(data as FuelConsumptionData))
        }

    override suspend fun deleteAllByProfileId(profileId: Long) =
        withContext(Dispatchers.Default) {
            dao.deleteAllByProfileId(profileId)
        }

    suspend fun selectLastMileage() =
        withContext(Dispatchers.Default) {
            dao.findLastMileage()
        }

    private fun convertToEntity(data: FuelConsumptionData) =
        FuelConsumptionEntity(
            data.id,
            data.fuelConsumption,
            data.litres,
            data.mileage,
            data.distance,
            data.time,
            data.profileId
        )

    private fun convertToData(entity: FuelConsumptionEntity) =
        FuelConsumptionData(
            entity.id!!,
            entity.time,
            entity.fuelConsumption,
            entity.litres,
            entity.mileage,
            entity.distance,
            entity.profileId
        )
}
