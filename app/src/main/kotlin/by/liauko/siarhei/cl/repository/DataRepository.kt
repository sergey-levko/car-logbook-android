package by.liauko.siarhei.cl.repository

import by.liauko.siarhei.cl.entity.AppData

interface DataRepository<T : AppData> : Repository<T> {
    suspend fun selectAllByProfileId(profileId: Long): List<T>
    suspend fun selectAllByProfileIdAndPeriod(profileId: Long): List<T>
    suspend fun deleteAllByProfileId(profileId: Long)
}
