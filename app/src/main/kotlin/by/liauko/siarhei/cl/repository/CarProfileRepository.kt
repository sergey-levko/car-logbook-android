package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.database.entity.CarProfileEntity
import by.liauko.siarhei.cl.entity.CarProfileData
import by.liauko.siarhei.cl.repository.converter.CarProfileConverter
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
            CarProfileConverter.convertToData(dao.findById(id))
        }

    override suspend fun selectAll() =
        withContext(Dispatchers.Default) {
            dao.findAll().map { CarProfileConverter.convertToData(it) }
        }

    override suspend fun insert(data: CarProfileData): Long =
        withContext(Dispatchers.Default) {
            dao.insert(CarProfileConverter.convertToEntity(data))
        }

    suspend fun insertAll(data: List<CarProfileEntity>) =
        withContext(Dispatchers.Default) {
            dao.insertAll(data)
        }

    override suspend fun update(data: CarProfileData) {
        withContext(Dispatchers.Default) {
            dao.update(CarProfileConverter.convertToEntity(data))
        }
    }

    override suspend fun delete(data: CarProfileData) {
        withContext(Dispatchers.Default) {
            dao.delete(CarProfileConverter.convertToEntity(data))
        }
    }

    suspend fun deleteAll() =
        withContext(Dispatchers.Default) {
            dao.deleteAll()
        }
}
