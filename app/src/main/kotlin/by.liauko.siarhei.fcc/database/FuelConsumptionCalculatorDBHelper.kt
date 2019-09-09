package by.liauko.siarhei.fcc.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns._ID
import by.liauko.siarhei.fcc.database.FCCContract.createEntries
import by.liauko.siarhei.fcc.database.FCCContract.deleteEntries
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameConsumption
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameDistance
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameLitres
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameTime
import by.liauko.siarhei.fcc.database.entry.FCCEntry.tableName

private const val dbVersion = 1
private const val dbName = "FuelConsumptionCalculator.db"

class FuelConsumptionCalculatorDBHelper(context: Context) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createEntries)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(deleteEntries)
        onCreate(db)
    }
}

object FCCContract {
    const val createEntries = "CREATE TABLE $tableName ($_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "$columnNameLitres REAL, $columnNameDistance REAL, $columnNameConsumption REAL," +
            "$columnNameTime INTEGER)"
    const val deleteEntries = "DROP TABLE IF EXISTS $tableName"
}