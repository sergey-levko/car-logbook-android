package by.liauko.siarhei.fcc.database.entry

import android.provider.BaseColumns

object FCCEntry : BaseColumns {
    const val tableName = "FUEL_CONSUMPTION"
    const val columnNameLitres = "LITRES"
    const val columnNameDistance = "DISTANCE"
    const val columnNameConsumption = "CONSUMPTION"
    const val columnNameTime = "TIME"
}