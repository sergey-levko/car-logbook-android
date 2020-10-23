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
            database.execSQL("ALTER TABLE log ADD COLUMN profile_id INTEGER")
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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE fuel_consumption RENAME TO fuel_consumption_tmp")
            database.execSQL("""
                CREATE TABLE fuel_consumption (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fuel_consumption REAL NOT NULL,
                    litres REAL NOT NULL,
                    distance REAL NOT NULL,
                    time INTEGER NOT NULL,
                    profile_id INTEGER NOT NULL
                )
            """.trimMargin())
            database.execSQL("""
                INSERT INTO fuel_consumption (id, fuel_consumption, litres, distance, time, profile_id)
                SELECT id, fuel_consumption, litres, distance, time, profile_id
                FROM fuel_consumption_tmp
            """.trimIndent())
            database.execSQL("DROP TABLE fuel_consumption_tmp")
            database.execSQL("ALTER TABLE fuel_consumption ADD COLUMN mileage INTEGER NOT NULL DEFAULT -1")
            database.execSQL("CREATE INDEX fuel_consumption_profile_id_idx ON fuel_consumption(profile_id)")

            database.execSQL("ALTER TABLE log RENAME TO log_tmp")
            database.execSQL("""
                CREATE TABLE log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    text TEXT,
                    mileage INTEGER NOT NULL,
                    time INTEGER NOT NULL,
                    profile_id INTEGER NOT NULL
                )
            """.trimIndent())
            database.execSQL("""
                INSERT INTO log (id, title, text, mileage, time, profile_id)
                SELECT id, title, text, mileage, time, profile_id
                FROM log_tmp
            """.trimIndent())
            database.execSQL("DROP TABLE log_tmp")
            database.execSQL("CREATE INDEX log_profile_id_idx ON log(profile_id)")
        }
    }
}
