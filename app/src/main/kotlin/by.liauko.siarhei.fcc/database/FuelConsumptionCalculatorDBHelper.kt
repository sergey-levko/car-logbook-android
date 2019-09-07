package by.liauko.siarhei.fcc.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import by.liauko.siarhei.fcc.database.FCCContract.createEntries
import by.liauko.siarhei.fcc.database.FCCContract.deleteEntries

private const val dbVersion = 1
private const val dbName = "FuelConsumptionCalculator.db"

class FuelConsumptionCalculatorDBHelper(context: Context) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(createEntries)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL(deleteEntries)
        onCreate(db)
    }
}

object FCCEntry : BaseColumns {
    const val tableName = "FUEL_CONSUMPTION"
    const val columnNameLitres = "LITRES"
    const val columnNameDistance = "DISTANCE"
    const val columnNameConsumption = "CONSUMPTION"
    const val columnNameTime = "TIME"
}

object FCCContract {
    const val createEntries = "CREATE TABLE ${FCCEntry.tableName} (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
            "${FCCEntry.columnNameLitres} REAL, ${FCCEntry.columnNameDistance} REAL, ${FCCEntry.columnNameConsumption} REAL," +
            "${FCCEntry.columnNameTime} INTEGER)"
    const val deleteEntries = "DROP TABLE IF EXISTS ${FCCEntry.tableName}"
}