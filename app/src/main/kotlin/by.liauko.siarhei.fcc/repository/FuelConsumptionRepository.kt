package by.liauko.siarhei.fcc.repository

import android.content.Context
import android.os.AsyncTask
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.database.dao.FuelConsumptionDao
import by.liauko.siarhei.fcc.database.entity.AppEntity
import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.fcc.entity.AppData
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.util.ApplicationUtil.periodCalendar
import by.liauko.siarhei.fcc.util.DataType

class FuelConsumptionRepository(context: Context) : Repository {

    private val database = CarLogDatabase(context)
    private val type = DataType.FUEL

    override fun selectAll() =
        SelectAsyncTask(type, database).execute().get().map { convertToData(it as FuelConsumptionEntity) }

    override fun selectAllByPeriod(): List<FuelConsumptionData> {
        val timeBounds = prepareDateRange(periodCalendar)
        return SelectByDateAsyncTask(type, database)
            .execute(timeBounds.first, timeBounds.second)
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

    private fun convertToEntity(fuelConsumptionData: FuelConsumptionData) =
        FuelConsumptionEntity(
            fuelConsumptionData.id,
            fuelConsumptionData.fuelConsumption,
            fuelConsumptionData.litres,
            fuelConsumptionData.distance,
            fuelConsumptionData.time
        )

    private fun convertToData(fuelConsumptionEntity: FuelConsumptionEntity) =
        FuelConsumptionData(
            fuelConsumptionEntity.id!!,
            fuelConsumptionEntity.time,
            fuelConsumptionEntity.fuelConsumption,
            fuelConsumptionEntity.litres,
            fuelConsumptionEntity.distance
        )
}
