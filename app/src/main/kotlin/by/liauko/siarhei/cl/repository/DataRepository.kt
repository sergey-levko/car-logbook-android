package by.liauko.siarhei.cl.repository

import by.liauko.siarhei.cl.entity.AppData

interface DataRepository : Repository<AppData> {
    suspend fun selectAllByProfileId(profileId: Long): List<AppData>
    suspend fun selectAllByProfileIdAndPeriod(profileId: Long): List<AppData>
    suspend fun deleteAllByProfileId(profileId: Long)
}
