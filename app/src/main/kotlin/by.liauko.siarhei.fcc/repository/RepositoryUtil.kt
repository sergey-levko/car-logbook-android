package by.liauko.siarhei.fcc.repository

import android.content.Context
import by.liauko.siarhei.fcc.util.ApplicationUtil
import by.liauko.siarhei.fcc.util.DataPeriod
import by.liauko.siarhei.fcc.util.DataType
import java.util.Calendar

object RepositoryUtil {
    fun prepareDateRange(calendar: Calendar): Pair<Long, Long> {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)

        val dayType = when (ApplicationUtil.dataPeriod) {
            DataPeriod.MONTH -> Calendar.DAY_OF_MONTH
            DataPeriod.YEAR -> Calendar.DAY_OF_YEAR
        }

        calendar.set(dayType, 1)
        val startTime = calendar.timeInMillis
        calendar.set(dayType, calendar.getActualMaximum(dayType))
        val endTime = calendar.timeInMillis

        return Pair(startTime, endTime)
    }
}

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