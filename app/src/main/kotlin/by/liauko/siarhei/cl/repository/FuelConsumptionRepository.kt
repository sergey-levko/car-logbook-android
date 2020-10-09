package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.AppEntity
import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.entity.FuelConsumptionData
import by.liauko.siarhei.cl.util.ApplicationUtil.periodCalendar
import by.liauko.siarhei.cl.util.DataType

class FuelConsumptionRepository(context: Context) : DataRepository {

    private val database = CarLogbookDatabase(context)
    private val type = DataType.FUEL

    override fun selectAll() =
        SelectAsyncTask(type, database).execute().get().map { convertToData(it as FuelConsumptionEntity) }

    override fun selectAllByProfileId(profileId: Long) =
        SelectByProfileIdAsyncTask(type, database).execute(profileId).get().map { convertToData(it as FuelConsumptionEntity) }

    override fun selectAllByProfileIdAndPeriod(profileId: Long): List<FuelConsumptionData> {
        val timeBounds = prepareDateRange(periodCalendar)
        return SelectByProfileIdAndDateAsyncTask(type, database)
            .execute(profileId, timeBounds.first, timeBounds.second)
            .get()
            .map { convertToData(it as FuelConsumptionEntity) }
    }

    override fun insert(entity: AppEntity): Long =
        InsertAsyncTask(type, database).execute(entity as FuelConsumptionEntity).get()

    override fun update(data: AppData) {
        UpdateAsyncTask(type, database).execute(convertToEntity(data as FuelConsumptionData))
    }

    override fun delete(data: AppData) {
        DeleteAsyncTask(type, database).execute(convertToEntity(data as FuelConsumptionData))
    }

    override fun deleteAllByProfileId(profileId: Long) {
        DeleteAllByProfileId(type, database).execute(profileId)
    }

    private fun convertToEntity(data: FuelConsumptionData) =
        FuelConsumptionEntity(
            data.id,
            data.fuelConsumption,
            data.litres,
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
            entity.distance,
            entity.profileId
        )
}
