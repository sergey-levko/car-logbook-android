package by.liauko.siarhei.cl.repository

interface Repository<Data> {
    suspend fun selectAll(): List<Data>
    suspend fun insert(data: Data): Long
    suspend fun update(data: Data)
    suspend fun delete(data: Data)
}
