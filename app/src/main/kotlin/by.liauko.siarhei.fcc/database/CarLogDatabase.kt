package by.liauko.siarhei.fcc.database

import androidx.room.Database
import androidx.room.RoomDatabase
import by.liauko.siarhei.fcc.database.dao.FuelConsumptionDao
import by.liauko.siarhei.fcc.database.dao.LogDao
import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.fcc.database.entity.LogEntity

@Database(entities = [LogEntity::class, FuelConsumptionEntity::class], version = 1)
abstract class CarLogDatabase: RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun fuelConsumptionDao(): FuelConsumptionDao
}