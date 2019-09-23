package by.liauko.siarhei.fcc.database.util

import android.os.AsyncTask
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.fcc.database.entity.LogEntity
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.entity.LogData

class CarLogDBUtil(database: CarLogDatabase) {
    private val logDao = database.logDao()
    private val fuelConsumptionDao = database.fuelConsumptionDao()

    fun selectLogData(): List<LogData> {
        class SelectAsyncTask: AsyncTask<Unit, Unit, List<LogEntity>>() {
            override fun doInBackground(vararg params: Unit?): List<LogEntity> = logDao.findAll()
        }
        val items = mutableListOf<LogData>() as ArrayList
        val entities = SelectAsyncTask().execute().get()
        for (entity in entities) {
            items.add(LogData(entity.id!!, entity.time, entity.title, entity.text, entity.mileage))
        }

        return items
    }

    fun selectFuelConsumptionData(): List<FuelConsumptionData> {
        class SelectAsyncTask: AsyncTask<Unit, Unit, List<FuelConsumptionEntity>>() {
            override fun doInBackground(vararg params: Unit?): List<FuelConsumptionEntity> = fuelConsumptionDao.findAll()
        }
        val items = mutableListOf<FuelConsumptionData>() as ArrayList
        val entities = SelectAsyncTask().execute().get()
        for (entity in entities) {
            items.add(FuelConsumptionData(entity.id!!, entity.time, entity.fuelConsumption, entity.litres, entity.distance))
        }

        return items
    }

    fun insertLogEntity(title: String,
                        text: String,
                        mileage: Long,
                        time: Long): Long {
        class InsertAsyncTask: AsyncTask<LogEntity, Unit, Long>() {
            override fun doInBackground(vararg params: LogEntity?): Long {
                val param = params[0]
                return if (param != null) logDao.insert(param) else -1L
            }
        }
        return InsertAsyncTask().execute(LogEntity(null, title, text, mileage, time)).get()
    }

    fun insertFuelConsumptionEntity(fuelConsumption: Double,
                                    litres: Double,
                                    distance: Double,
                                    time: Long): Long {
        class InsertAsyncTask: AsyncTask<FuelConsumptionEntity, Unit, Long>() {
            override fun doInBackground(vararg params: FuelConsumptionEntity?): Long {
                val param = params[0]
                return if (param != null) fuelConsumptionDao.insert(param) else -1L
            }
        }
        return InsertAsyncTask().execute(FuelConsumptionEntity(null, fuelConsumption, litres, distance, time)).get()
    }

    fun updateLogData(logData: LogData) {
        class UpdateAsyncTask: AsyncTask<LogData, Unit, Unit>() {
            override fun doInBackground(vararg params: LogData?) {
                logDao.update(LogEntity(logData.id, logData.title, logData.text, logData.mileage, logData.time))
            }
        }
        UpdateAsyncTask().execute(logData)
    }

    fun updateFuelConsumptionData(fuelConsumptionData: FuelConsumptionData) {
        class UpdateAsyncTask: AsyncTask<FuelConsumptionData, Unit, Unit>() {
            override fun doInBackground(vararg params: FuelConsumptionData?) {
                fuelConsumptionDao.update(FuelConsumptionEntity(fuelConsumptionData.id,
                    fuelConsumptionData.fuelConsumption, fuelConsumptionData.litres, fuelConsumptionData.distance, fuelConsumptionData.time))
            }
        }
        UpdateAsyncTask().execute(fuelConsumptionData)
    }

    fun deleteLogData(logData: LogData) {
        class DeleteAsyncTask: AsyncTask<LogData, Unit, Unit>() {
            override fun doInBackground(vararg params: LogData?) {
                val param = params[0]
                if (param != null) {
                    logDao.delete(
                        LogEntity(
                            param.id,
                            param.title,
                            param.text,
                            param.mileage,
                            param.time
                        )
                    )
                }
            }
        }
        DeleteAsyncTask().execute(logData)
    }

    fun deleteFuelConsumptionData(fuelConsumptionData: FuelConsumptionData) {
        class DeleteAsyncTask: AsyncTask<FuelConsumptionData, Unit, Unit>() {
            override fun doInBackground(vararg params: FuelConsumptionData?) {
                val param = params[0]
                if (param != null) {
                    fuelConsumptionDao.delete(
                        FuelConsumptionEntity(
                            param.id,
                            param.fuelConsumption,
                            param.litres,
                            param.distance,
                            param.time
                        )
                    )
                }
            }
        }
        DeleteAsyncTask().execute(fuelConsumptionData)
    }
}