package by.liauko.siarhei.fcc.repository

import android.content.Context
import android.os.AsyncTask
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.database.dao.FuelConsumptionDao
import by.liauko.siarhei.fcc.database.entity.AppEntity
import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.fcc.entity.AppData
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.util.ApplicationUtil.period

class FuelConsumptionRepository(context: Context): Repository {
    private val fuelConsumptionDao: FuelConsumptionDao

    init {
        val database = CarLogDatabase(context)
        fuelConsumptionDao = database.fuelConsumptionDao()
    }

    override fun selectAll(): List<FuelConsumptionData> {
        val items = mutableListOf<FuelConsumptionData>() as ArrayList
        val entities = SelectAsyncTask(fuelConsumptionDao).execute().get()
        for (entity in entities) {
            items.add(convertToData(entity))
        }

        return items
    }

    override fun selectAllByPeriod(): List<FuelConsumptionData> {
        val timeBounds = RepositoryUtil.prepareDateRange(period)
        val items = mutableListOf<FuelConsumptionData>() as ArrayList
        val entities = SelectByDateAsyncTask(fuelConsumptionDao)
            .execute(timeBounds.first, timeBounds.second)
            .get()
        for (entity in entities) {
            items.add(convertToData(entity))
        }

        return items
    }

    override fun insert(entity: AppEntity) =
        InsertAsyncTask(fuelConsumptionDao).execute(entity as FuelConsumptionEntity).get()

    override fun update(data: AppData) {
        UpdateAsyncTask(fuelConsumptionDao).execute(convertToEntity(data as FuelConsumptionData))
    }

    override fun delete(data: AppData) {
        DeleteAsyncTask(fuelConsumptionDao).execute(convertToEntity(data as FuelConsumptionData))
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

    private class SelectAsyncTask(private val logDao: FuelConsumptionDao): AsyncTask<Unit, Unit, List<FuelConsumptionEntity>>() {
        override fun doInBackground(vararg params: Unit?): List<FuelConsumptionEntity> = logDao.findAll()
    }

    private class SelectByDateAsyncTask(private val fuelConsumptionDao: FuelConsumptionDao): AsyncTask<Long, Unit, List<FuelConsumptionEntity>>() {
        override fun doInBackground(vararg params: Long?): List<FuelConsumptionEntity> = fuelConsumptionDao.findAllByDate(params[0]!!, params[1]!!)
    }

    private class InsertAsyncTask(private val logDao: FuelConsumptionDao): AsyncTask<FuelConsumptionEntity, Unit, Long>() {
        override fun doInBackground(vararg params: FuelConsumptionEntity?) = logDao.insert(params[0]!!)
    }

    private class UpdateAsyncTask(private val logDao: FuelConsumptionDao): AsyncTask<FuelConsumptionEntity, Unit, Unit>() {
        override fun doInBackground(vararg params: FuelConsumptionEntity?) {
            logDao.update(params[0]!!)
        }
    }

    private class DeleteAsyncTask(private val logDao: FuelConsumptionDao): AsyncTask<FuelConsumptionEntity, Unit, Unit>() {
        override fun doInBackground(vararg params: FuelConsumptionEntity?) {
            logDao.delete(params[0]!!)
        }
    }
}
