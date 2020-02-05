package by.liauko.siarhei.cl.repository

import by.liauko.siarhei.cl.database.entity.AppEntity
import by.liauko.siarhei.cl.entity.AppData

interface Repository {
    fun selectAll(): List<AppData>
    fun selectAllByPeriod(): List<AppData>
    fun insert(entity: AppEntity): Long
    fun update(data: AppData)
    fun delete(data: AppData)
}