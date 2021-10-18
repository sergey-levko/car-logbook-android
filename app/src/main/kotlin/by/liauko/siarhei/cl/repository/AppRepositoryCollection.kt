package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.util.DataType

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
