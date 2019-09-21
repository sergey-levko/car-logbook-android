package by.liauko.siarhei.fcc.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns._ID
import by.liauko.siarhei.fcc.database.FCCContract.createFuelTable
import by.liauko.siarhei.fcc.database.FCCContract.createLogTable
import by.liauko.siarhei.fcc.database.FCCContract.deleteFuelTable
import by.liauko.siarhei.fcc.database.FCCContract.deleteLogTable
import by.liauko.siarhei.fcc.database.entry.FuelEntry
import by.liauko.siarhei.fcc.database.entry.FuelEntry.columnNameConsumption
import by.liauko.siarhei.fcc.database.entry.FuelEntry.columnNameDistance
import by.liauko.siarhei.fcc.database.entry.FuelEntry.columnNameLitres
import by.liauko.siarhei.fcc.database.entry.FuelEntry.fuelTableName
import by.liauko.siarhei.fcc.database.entry.LogEntry
import by.liauko.siarhei.fcc.database.entry.LogEntry.columnNameMileage
import by.liauko.siarhei.fcc.database.entry.LogEntry.columnNameText
import by.liauko.siarhei.fcc.database.entry.LogEntry.columnNameTitle
import by.liauko.siarhei.fcc.database.entry.LogEntry.logTableName

private const val dbVersion = 1
private const val dbName = "CarLog.db"

class FuelConsumptionCalculatorDBHelper(context: Context) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createFuelTable)
        db?.execSQL(createLogTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(deleteFuelTable)
        db?.execSQL(deleteLogTable)
        onCreate(db)
    }
}

object FCCContract {
    const val createFuelTable = "CREATE TABLE $fuelTableName ($_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "$columnNameLitres REAL, $columnNameDistance REAL, $columnNameConsumption REAL," +
            "${FuelEntry.columnNameTime} INTEGER)"
    const val createLogTable = "CREATE TABLE $logTableName ($_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "$columnNameTitle TEXT, $columnNameText TEXT, ${LogEntry.columnNameTime} INTEGER," +
            "$columnNameMileage INTEGER)"
    const val deleteFuelTable = "DROP TABLE IF EXISTS $fuelTableName"
    const val deleteLogTable = "DROP TABLE IF EXISTS $logTableName"
}