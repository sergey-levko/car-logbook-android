package by.liauko.siarhei.cl.repository

interface Repository<Data, Entity> {
    fun selectAll(): List<Data>
    fun insert(entity: Entity): Long
    fun update(data: Data)
    fun delete(data: Data)
}