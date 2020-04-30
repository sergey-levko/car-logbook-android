package by.liauko.siarhei.fcc.repository

import android.content.Context
import by.liauko.siarhei.fcc.entity.DataType

class AppRepositoryCollection(context: Context) {
    private val logRepository = LogRepository(context)
    private val fuelConsumptionRepository = FuelConsumptionRepository(context)

    fun getRepository(type: DataType): Repository {
        return when (type) {
            DataType.LOG -> logRepository
            DataType.FUEL -> fuelConsumptionRepository
        }
    }
}