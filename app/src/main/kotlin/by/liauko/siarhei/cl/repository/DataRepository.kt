package by.liauko.siarhei.cl.repository

import by.liauko.siarhei.cl.database.entity.AppEntity
import by.liauko.siarhei.cl.entity.AppData

interface DataRepository : Repository<AppData, AppEntity> {
    fun selectAllByProfileId(profileId: Long): List<AppData>
    fun selectAllByProfileIdAndPeriod(profileId: Long): List<AppData>
    fun deleteAllByProfileId(profileId: Long)
}
