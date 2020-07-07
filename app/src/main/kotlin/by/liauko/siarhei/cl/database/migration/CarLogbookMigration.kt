package by.liauko.siarhei.cl.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object CarLogbookMigration {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE car_profile (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    body_type TEXT NOT NULL,
                    fuel_type TEXT NOT NULL,
                    engine_volume REAL
                )
            """.trimIndent())
            database.execSQL("ALTER TABLE fuel_consumption ADD COLUMN profile_id INTEGER")
            database.execSQL("ALTER TABLE log ADD COLUMN profile_id INTEGER ")
            database.execSQL("""
                INSERT INTO car_profile (name, body_type, fuel_type, engine_volume) 
                VALUES ('Default Car Profile', 'SEDAN', 'GASOLINE', 1.0)
            """.trimIndent())
            database.execSQL("""
                UPDATE fuel_consumption 
                SET profile_id = (SELECT id FROM car_profile WHERE name = 'Default Car Profile')
            """.trimIndent())
            database.execSQL("""
                UPDATE log 
                SET profile_id = (SELECT id FROM car_profile WHERE name = 'Default Car Profile')
            """.trimIndent())
        }
    }
}