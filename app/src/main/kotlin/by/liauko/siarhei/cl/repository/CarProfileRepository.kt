package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.CarProfileEntity
import by.liauko.siarhei.cl.model.CarProfileModel
import by.liauko.siarhei.cl.repository.converter.CarProfileConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext

class CarProfileRepository(context: Context) :
    Repository<CarProfileModel>,
    CoroutineScope by MainScope()
{

    private val dao = CarLogbookDatabase(context).carProfileDao()

    suspend fun selectById(id: Long) =
        withContext(Dispatchers.Default) {
            CarProfileConverter.convertToData(dao.findById(id))
        }

    override suspend fun selectAll() =
        withContext(Dispatchers.Default) {
            dao.findAll().map { CarProfileConverter.convertToData(it) }
        }

    override suspend fun insert(model: CarProfileModel): Long =
        withContext(Dispatchers.Default) {
            dao.insert(CarProfileConverter.convertToEntity(model))
        }

    suspend fun insertAll(data: List<CarProfileEntity>) =
        withContext(Dispatchers.Default) {
            dao.insertAll(data)
        }

    override suspend fun update(model: CarProfileModel) {
        withContext(Dispatchers.Default) {
            dao.update(CarProfileConverter.convertToEntity(model))
        }
    }

    override suspend fun delete(model: CarProfileModel) {
        withContext(Dispatchers.Default) {
            dao.delete(CarProfileConverter.convertToEntity(model))
        }
    }

    override suspend fun deleteAll() =
        withContext(Dispatchers.Default) {
            dao.deleteAll()
        }
}
