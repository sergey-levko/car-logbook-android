package by.liauko.siarhei.cl.repository

interface DataRepository<T> : Repository<T> {
    suspend fun selectAllByProfileId(profileId: Long): List<T>
    suspend fun selectAllByProfileIdAndPeriod(profileId: Long): List<T>
    suspend fun deleteAllByProfileId(profileId: Long)
}
