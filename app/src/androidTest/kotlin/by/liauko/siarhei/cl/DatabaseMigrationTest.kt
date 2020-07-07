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
        assertEquals("Number of car profiles must be 1", profiles.size, 1)
        assertEquals("Wrong car profile name", profiles[0].name, "Default Car Profile")
        assertEquals("Wrong car profile body type", profiles[0].bodyType, "SEDAN")
        assertEquals("Wrong car profile fuel type", profiles[0].fuelType, "GASOLINE")
        assertEquals("Wrong car profile engine volume", profiles[0].engineVolume, 1.0)

        val fuelConsumptions = db.fuelConsumptionDao().findAll()
        assertEquals("Number of fuel consumptions must be 1", fuelConsumptions.size, 1)
        assertEquals("Wrong fuel consumption value", fuelConsumptions[0].fuelConsumption, 14.0, 0.0)
        assertEquals("Wrong litres value", fuelConsumptions[0].litres, 14.0, 0.0)
        assertEquals("Wrong distance value", fuelConsumptions[0].distance, 100.0, 0.0)
        assertEquals("Wrong fuel consumption time", fuelConsumptions[0].time, time)
        assertEquals("Wrong car profile id", fuelConsumptions[0].profileId, profiles[0].id)

        val logs = db.logDao().findAll()
        assertEquals("Number of logs must be 1", logs.size, 1)
        assertEquals("Wrong fuel consumption value", logs[0].title, "Title")
        assertEquals("Wrong litres value", logs[0].text, "It is log text")
        assertEquals("Wrong distance value", logs[0].mileage, 32010)
        assertEquals("Wrong fuel consumption time", logs[0].time, time)
        assertEquals("Wrong car profile id", logs[0].profileId, profiles[0].id)
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