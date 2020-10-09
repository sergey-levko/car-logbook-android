package by.liauko.siarhei.cl

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.migration.CarLogbookMigration.MIGRATION_1_2
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

        helper.runMigrationsAndValidate(testDb, 2, true, MIGRATION_1_2)

        val db = getMigratedRoomDatabase()

        val profiles = db.carProfileDao().findAll()
        assertEquals("Number of car profiles must be 1", 1, profiles.size)
        assertEquals("Wrong car profile name", "Default Car Profile", profiles[0].name)
        assertEquals("Wrong car profile body type", "SEDAN", profiles[0].bodyType)
        assertEquals("Wrong car profile fuel type", "GASOLINE", profiles[0].fuelType)
        assertEquals("Wrong car profile engine volume", 1.0, profiles[0].engineVolume)

        val fuelConsumptions = db.fuelConsumptionDao().findAll()
        assertEquals("Number of fuel consumptions must be 1", 1, fuelConsumptions.size)
        assertEquals("Wrong fuel consumption value", 14.0, fuelConsumptions[0].fuelConsumption, 0.0)
        assertEquals("Wrong litres value", 14.0, fuelConsumptions[0].litres, 0.0)
        assertEquals("Wrong distance value", 100.0, fuelConsumptions[0].distance, 0.0)
        assertEquals("Wrong fuel consumption time", time, fuelConsumptions[0].time)
        assertEquals("Wrong car profile id", profiles[0].id, fuelConsumptions[0].profileId)

        val logs = db.logDao().findAll()
        assertEquals("Number of logs must be 1", 1, logs.size)
        assertEquals("Wrong fuel consumption value", "Title", logs[0].title)
        assertEquals("Wrong litres value", "It is log text", logs[0].text)
        assertEquals("Wrong distance value", 32010, logs[0].mileage)
        assertEquals("Wrong fuel consumption time", time, logs[0].time)
        assertEquals("Wrong car profile id", profiles[0].id, logs[0].profileId)
    }

    private fun getMigratedRoomDatabase(): CarLogbookDatabase {
        val db = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            CarLogbookDatabase::class.java,
            testDb
        ).addMigrations(MIGRATION_1_2)
            .build()

        helper.closeWhenFinished(db)
        return  db
    }
}