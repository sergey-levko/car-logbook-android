package by.liauko.siarhei.fcc.database.util

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns._ID
import by.liauko.siarhei.fcc.database.entry.FuelEntry
import by.liauko.siarhei.fcc.database.entry.FuelEntry.columnNameConsumption
import by.liauko.siarhei.fcc.database.entry.FuelEntry.columnNameDistance
import by.liauko.siarhei.fcc.database.entry.FuelEntry.columnNameLitres
import by.liauko.siarhei.fcc.database.entry.FuelEntry.fuelTableName
import by.liauko.siarhei.fcc.database.entry.LogEntry
import by.liauko.siarhei.fcc.database.entry.LogEntry.columnNameMileage
import by.liauko.siarhei.fcc.database.entry.LogEntry.columnNameText
import by.liauko.siarhei.fcc.database.entry.LogEntry.columnNameTime
import by.liauko.siarhei.fcc.database.entry.LogEntry.columnNameTitle
import by.liauko.siarhei.fcc.database.entry.LogEntry.logTableName
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.entity.LogData

class CarLogDBUtil(private val database: SQLiteDatabase) {

    fun selectFuelData(): List<FuelConsumptionData> {
        val items = mutableListOf<FuelConsumptionData>() as ArrayList
        database.query(fuelTableName, null, "", emptyArray(), null, null, null).use {
            if (it.moveToFirst()) {
                val idColumnIndex = it.getColumnIndex(_ID)
                val litresColumnIndex = it.getColumnIndex(columnNameLitres)
                val distanceColumnIndex = it.getColumnIndex(columnNameDistance)
                val consumptionColumnIndex = it.getColumnIndex(columnNameConsumption)
                val timeColumnIndex = it.getColumnIndex(FuelEntry.columnNameTime)

                do {
                    items.add(
                        FuelConsumptionData(
                            it.getLong(idColumnIndex),
                            it.getLong(timeColumnIndex),
                            it.getDouble(consumptionColumnIndex),
                            it.getDouble(litresColumnIndex),
                            it.getDouble(distanceColumnIndex)
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return items
    }

    fun selectLogData(): List<LogData> {
        val items = mutableListOf<LogData>() as ArrayList
        database.query(logTableName, null, "", emptyArray(), null, null, null).use {
            if (it.moveToFirst()) {
                val idColumnIndex = it.getColumnIndex(_ID)
                val titleColumnIndex = it.getColumnIndex(columnNameTitle)
                val textColumnIndex = it.getColumnIndex(columnNameText)
                val timeColumnIndex = it.getColumnIndex(LogEntry.columnNameTime)
                val mileageColumnIndex = it.getColumnIndex(columnNameMileage)

                do {
                    items.add(
                        LogData(
                            it.getLong(idColumnIndex),
                            it.getLong(timeColumnIndex),
                            it.getString(titleColumnIndex),
                            it.getString(textColumnIndex),
                            it.getLong(mileageColumnIndex)
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return items
    }

    fun insertFuelData(litres: Double,
                       distance: Double,
                       fuelConsumption: Double,
                       time: Long)
            = database.insert(fuelTableName, null, fillFuelValues(litres, distance, fuelConsumption, time))

    fun insertLogData(title: String,
                      text: String,
                      time: Long,
                      mileage: Long)
            = database.insert(logTableName, null, fillLogValues(title, text, time, mileage))

    fun updateFuelItem(item: FuelConsumptionData) {
        database.update(fuelTableName,
            fillFuelValues(item.litres, item.distance, item.fuelConsumption, item.time),
            "$_ID LIKE ?",
            arrayOf(item.id.toString()))
    }

    fun updateLogItem(item: LogData) {
        database.update(logTableName,
            fillLogValues(item.title, item.text, item.time, item.mileage),
            "$_ID LIKE ?",
            arrayOf(item.id.toString()))
    }

    private fun fillFuelValues(litres: Double,
                               distance: Double,
                               fuelConsumption: Double,
                               time: Long)
            : ContentValues {

        val values = ContentValues()
        values.put(columnNameLitres, litres)
        values.put(columnNameDistance, distance)
        values.put(columnNameConsumption, fuelConsumption)
        values.put(columnNameTime, time)
        return values
    }

    private fun fillLogValues(title: String,
                              text: String,
                              time: Long,
                              mileage: Long)
            : ContentValues {

        val values = ContentValues()
        values.put(columnNameTitle, title)
        values.put(columnNameText, text)
        values.put(columnNameTime, time)
        values.put(columnNameMileage, mileage)
        return values
    }
}