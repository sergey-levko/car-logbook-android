package by.liauko.siarhei.cl

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.migration.CarLogbookMigration.MIGRATION_1_2
import by.liauko.siarhei.cl.database.migration.CarLogbookMigration.MIGRATION_2_3
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4ClassRunner::class)
class DatabaseMigrationTest {

    private val testDb = "test_db"

    @Rule
    @JvmField
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CarLogbookDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2() {
        val time = Calendar.getInstance().timeInMillis
        helper.createDatabase(testDb, 1).apply {
            execSQL("""
                INSERT INTO fuel_consumption (fuel_consumption, litres, distance, time) 
                VALUES (14.0, 14.0, 100.0, :time)
            """.trimIndent(),
                arrayOf(time))
            execSQL("""
                INSERT INTO log (title, text, mileage, time) 
                VALUES ('Title', 'It is log text', 32010, :time)
            """.trimIndent(),
                arrayOf(time))

            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 2, true, MIGRATION_1_2)

        var cursor = db.query("SELECT id, name, body_type, fuel_type, engine_volume FROM car_profile")
        assertEquals("Number of car profiles must be 1", 1, cursor.count)
        cursor.moveToFirst()
        val profileId = cursor.getInt(0)
        assertEquals("Wrong car profile name", "Default Car Profile", cursor.getString(1))
        assertEquals("Wrong car profile body type", "SEDAN", cursor.getString(2))
        assertEquals("Wrong car profile fuel type", "GASOLINE", cursor.getString(3))
        assertEquals("Wrong car profile engine volume", 1.0, cursor.getDouble(4), 0.0)

        cursor = db.query("SELECT fuel_consumption, litres, distance, time, profile_id FROM fuel_consumption")
        assertEquals("Number of fuel consumptions must be 1", 1, cursor.count)
        cursor.moveToFirst()
        assertEquals("Wrong fuel consumption value", 14.0, cursor.getDouble(0), 0.0)
        assertEquals("Wrong litres value", 14.0, cursor.getDouble(1), 0.0)
        assertEquals("Wrong distance value", 100.0, cursor.getDouble(2), 0.0)
        assertEquals("Wrong fuel consumption time", time, cursor.getLong(3))
        assertEquals("Wrong car profile id", profileId, cursor.getInt(4))

        cursor = db.query("SELECT title, text, mileage, time, profile_id FROM log")
        assertEquals("Number of logs must be 1", 1, cursor.count)
        cursor.moveToFirst()
        assertEquals("Wrong log title value", "Title", cursor.getString(0))
        assertEquals("Wrong log text value", "It is log text", cursor.getString(1))
        assertEquals("Wrong log mileage value", 32010, cursor.getInt(2))
        assertEquals("Wrong log time", time, cursor.getLong(3))
        assertEquals("Wrong car profile id", profileId, cursor.getInt(4))
    }

    @Test
    fun migrate2To3() {
        val time = Calendar.getInstance().timeInMillis
        helper.createDatabase(testDb, 2).apply {
            execSQL("""
                INSERT INTO fuel_consumption (fuel_consumption, litres, distance, time, profile_id) 
                VALUES (14.0, 14.0, 100.0, :time, 1)
            """.trimIndent(),
                arrayOf(time))
            execSQL("""
                INSERT INTO log (title, text, mileage, time, profile_id) 
                VALUES ('Title', 'It is log text', 32010, :time, 1)
            """.trimIndent(),
                arrayOf(time))

            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 3, true, MIGRATION_2_3)

        var cursor = db.query("SELECT fuel_consumption, litres, mileage, distance, time, profile_id FROM fuel_consumption")
        assertEquals("Number of fuel consumptions must be 1", 1, cursor.count)
        cursor.moveToFirst()
        assertEquals("Wrong fuel consumption value", 14.0, cursor.getDouble(0), 0.0)
        assertEquals("Wrong litres value", 14.0, cursor.getDouble(1), 0.0)
        assertEquals("Wrong mileage value", 0, cursor.getInt(2))
        assertEquals("Wrong distance value", 100.0, cursor.getDouble(3), 0.0)
        assertEquals("Wrong fuel consumption time", time, cursor.getLong(4))
        assertEquals("Wrong car profile id", 1, cursor.getInt(5))

        cursor = db.query("SELECT title, text, mileage, time, profile_id FROM log")
        assertEquals("Number of logs must be 1", 1, cursor.count)
        cursor.moveToFirst()
        assertEquals("Wrong log title value", "Title", cursor.getString(0))
        assertEquals("Wrong log text value", "It is log text", cursor.getString(1))
        assertEquals("Wrong log mileage value", 32010, cursor.getInt(2))
        assertEquals("Wrong log time", time, cursor.getLong(3))
        assertEquals("Wrong car profile id", 1, cursor.getInt(4))
    }
}