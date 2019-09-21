package by.liauko.siarhei.fcc.database.entry

import android.provider.BaseColumns

object LogEntry: BaseColumns {
    const val logTableName = "LOG"
    const val columnNameTitle = "TITLE"
    const val columnNameText = "TEXT"
    const val columnNameTime = "TIME"
    const val columnNameMileage = "MILEAGE"
}