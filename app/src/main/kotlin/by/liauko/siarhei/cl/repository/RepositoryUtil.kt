package by.liauko.siarhei.cl.repository

import android.content.Context
import android.os.AsyncTask
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.AppEntity
import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.cl.util.DataPeriod
import by.liauko.siarhei.cl.util.DataType
import java.util.Calendar

fun prepareDateRange(calendar: Calendar): Pair<Long, Long> {
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.clear(Calendar.MINUTE)
    calendar.clear(Calendar.SECOND)
    calendar.clear(Calendar.MILLISECOND)

    val dayType = when (dataPeriod) {
        DataPeriod.MONTH -> Calendar.DAY_OF_MONTH
        DataPeriod.YEAR -> Calendar.DAY_OF_YEAR
        DataPeriod.ALL -> 0
    }

    calendar.set(dayType, 1)
    val startTime = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY))
    calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE))
    calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND))
    calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND))
    calendar.set(dayType, calendar.getActualMaximum(dayType))
    val endTime = calendar.timeInMillis

    return Pair(startTime, endTime)
}

class AppRepositoryCollection(context: Context) {

    private val logRepository = LogRepository(context)
    private val fuelConsumptionRepository = FuelConsumptionRepository(context)

    fun getRepository(type: DataType): DataRepository {
        return when (type) {
            DataType.LOG -> logRepository
            DataType.FUEL -> fuelConsumptionRepository
        }
    }
}

class SelectAsyncTask(private val dataType: DataType, private val db: CarLogbookDatabase) : AsyncTask<Unit, Unit, List<AppEntity>>() {

    override fun doInBackground(vararg params: Unit?) =
        when (dataType) {
            DataType.LOG -> db.logDao().findAll()
            DataType.FUEL -> db.fuelConsumptionDao().findAll()
        }
}

class SelectByProfileIdAsyncTask(private val dataType: DataType, private val db: CarLogbookDatabase) : AsyncTask<Long, Unit, List<AppEntity>>() {

    override fun doInBackground(vararg params: Long?) =
        when (dataType) {
            DataType.LOG -> db.logDao().findAllByProfileId(params[0]!!)
            DataType.FUEL -> db.fuelConsumptionDao().findAllByProfileId(params[0]!!)
        }
}

class SelectByProfileIdAndDateAsyncTask(private val dataType: DataType, private val db: CarLogbookDatabase) : AsyncTask<Long, Unit, List<AppEntity>>() {

    override fun doInBackground(vararg params: Long?) =
        when (dataType) {
            DataType.LOG -> db.logDao().findAllByProfileIdAndDate(params[0]!!, params[1]!!, params[2]!!)
            DataType.FUEL -> db.fuelConsumptionDao().findAllByProfileIdAndDate(params[0]!!, params[1]!!, params[2]!!)
        }
}

class InsertAsyncTask(private val dataType: DataType, private val db: CarLogbookDatabase) : AsyncTask<AppEntity, Unit, Long>() {

    override fun doInBackground(vararg params: AppEntity?) =
        when (dataType) {
            DataType.LOG -> db.logDao().insert(params[0]!! as LogEntity)
            DataType.FUEL -> db.fuelConsumptionDao().insert(params[0]!! as FuelConsumptionEntity)
        }
}

class InsertAllAsyncTask(private val dataType: DataType, private val db: CarLogbookDatabase) : AsyncTask<List<AppEntity>, Unit, Unit>() {

    @Suppress("UNCHECKED_CAST")
    override fun doInBackground(vararg params: List<AppEntity>?) {
        when (dataType) {
            DataType.LOG -> db.logDao().insertAll(params[0]!! as List<LogEntity>)
            DataType.FUEL -> db.fuelConsumptionDao().insertAll(params[0]!! as List<FuelConsumptionEntity>)
        }
    }
}

class UpdateAsyncTask(private val dataType: DataType, private val db: CarLogbookDatabase) : AsyncTask<AppEntity, Unit, Unit>() {

    override fun doInBackground(vararg params: AppEntity?) {
        when (dataType) {
            DataType.LOG -> db.logDao().update(params[0]!! as LogEntity)
            DataType.FUEL -> db.fuelConsumptionDao().update(params[0]!! as FuelConsumptionEntity)
        }
    }
}

class DeleteAsyncTask(private val dataType: DataType, private val db: CarLogbookDatabase) : AsyncTask<AppEntity, Unit, Unit>() {

    override fun doInBackground(vararg params: AppEntity?) {
        when (dataType) {
            DataType.LOG -> db.logDao().delete(params[0]!! as LogEntity)
            DataType.FUEL -> db.fuelConsumptionDao().delete(params[0]!! as FuelConsumptionEntity)
        }
    }
}

class DeleteAllAsyncTask(private val dataType: DataType, private val db: CarLogbookDatabase) : AsyncTask<Unit, Unit, Unit>() {

    override fun doInBackground(vararg params: Unit?) {
        when (dataType) {
            DataType.LOG -> db.logDao().deleteAll()
            DataType.FUEL -> db.fuelConsumptionDao().deleteAll()
        }
    }

}

class DeleteAllByProfileId(private val dataType: DataType, private val db: CarLogbookDatabase) : AsyncTask<Long, Unit, Unit>() {

    override fun doInBackground(vararg params: Long?) {
        when (dataType) {
            DataType.LOG -> db.logDao().deleteAllByProfileId(params[0]!!)
            DataType.FUEL -> db.fuelConsumptionDao().deleteAllByProfileId(params[0]!!)
        }
    }
}
