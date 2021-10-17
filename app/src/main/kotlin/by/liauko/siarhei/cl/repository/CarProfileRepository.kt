package by.liauko.siarhei.cl.repository

import android.content.Context
import android.os.AsyncTask
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.CarProfileEntity
import by.liauko.siarhei.cl.entity.CarProfileData
import by.liauko.siarhei.cl.util.CarBodyType
import by.liauko.siarhei.cl.util.CarFuelType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext

class CarProfileRepository(context: Context) :
    Repository<CarProfileData>,
    CoroutineScope by MainScope()
{

    private val dao = CarLogbookDatabase(context).carProfileDao()

    suspend fun selectById(id: Long) =
        withContext(Dispatchers.Default) {
            convertToData(dao.findById(id))
        }

    override suspend fun selectAll() =
        withContext(Dispatchers.Default) {
            dao.findAll().map { convertToData(it) }
        }

    override suspend fun insert(data: CarProfileData): Long =
        withContext(Dispatchers.Default) {
            dao.insert(convertToEntity(data))
        }

    override suspend fun update(data: CarProfileData) {
        withContext(Dispatchers.Default) {
            dao.update(convertToEntity(data))
        }
    }

    override suspend fun delete(data: CarProfileData) {
        withContext(Dispatchers.Default) {
            dao.delete(convertToEntity(data))
        }
    }

    private fun convertToEntity(data: CarProfileData) =
        CarProfileEntity(
            data.id,
            data.name,
            data.bodyType.name,
            data.fuelType.name,
            data.engineVolume
        )

    private fun convertToData(entity: CarProfileEntity) =
        CarProfileData(
            entity.id!!,
            entity.name,
            CarBodyType.valueOf(entity.bodyType),
            CarFuelType.valueOf(entity.fuelType),
            entity.engineVolume
        )
}

class SelectAllCarProfileAsyncTask(private val db: CarLogbookDatabase) : AsyncTask<Unit, Unit, List<CarProfileEntity>>() {

    override fun doInBackground(vararg params: Unit?) =
        db.carProfileDao().findAll()
}

class InsertCarProfileAsyncTask(private val db: CarLogbookDatabase) : AsyncTask<CarProfileEntity, Unit, Long>() {

    override fun doInBackground(vararg params: CarProfileEntity?) =
        db.carProfileDao().insert(params[0]!!)
}

class InsertAllCarProfileAsyncTask(private val db: CarLogbookDatabase) : AsyncTask<List<CarProfileEntity>, Unit, Unit>() {

    @Suppress("UNCHECKED_CAST")
    override fun doInBackground(vararg params: List<CarProfileEntity>?) {
        db.carProfileDao().insertAll(params[0]!!)
    }
}

class DeleteAllCarProfilesAsyncTask(private val db: CarLogbookDatabase) : AsyncTask<Unit, Unit, Unit>() {

    override fun doInBackground(vararg params: Unit?) {
        db.carProfileDao().deleteAll()
    }

}
