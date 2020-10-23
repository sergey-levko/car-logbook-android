package by.liauko.siarhei.cl.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import by.liauko.siarhei.cl.database.dao.CarProfileDao
import by.liauko.siarhei.cl.database.dao.FuelConsumptionDao
import by.liauko.siarhei.cl.database.dao.LogDao
import by.liauko.siarhei.cl.database.entity.CarProfileEntity
import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.database.migration.CarLogbookMigration.MIGRATION_1_2
import by.liauko.siarhei.cl.database.migration.CarLogbookMigration.MIGRATION_2_3

@Database(entities = [LogEntity::class, FuelConsumptionEntity::class, CarProfileEntity::class], version = 3)
abstract class CarLogbookDatabase : RoomDatabase() {

    abstract fun logDao(): LogDao
    abstract fun fuelConsumptionDao(): FuelConsumptionDao
    abstract fun carProfileDao(): CarProfileDao

    companion object {
        private var instance: CarLogbookDatabase? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, CarLogbookDatabase::class.java, "car-log")
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build()

        fun closeDatabase() {
            instance ?: synchronized(lock) {
                instance!!.close()
                instance = null
            }
        }
    }
}
