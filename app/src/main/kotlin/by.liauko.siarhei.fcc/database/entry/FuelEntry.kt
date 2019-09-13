package by.liauko.siarhei.fcc.database.entry

import android.provider.BaseColumns

object FuelEntry: BaseColumns {
    const val fuelTableName = "FUEL_CONSUMPTION"
    const val columnNameLitres = "LITRES"
    const val columnNameDistance = "DISTANCE"
    const val columnNameConsumption = "CONSUMPTION"
    const val columnNameTime = "TIME"
}