package by.liauko.siarhei.fcc.repository

import by.liauko.siarhei.fcc.database.entity.AppEntity
import by.liauko.siarhei.fcc.entity.AppData
import java.util.Calendar

interface Repository {
    fun selectAll(): List<AppData>
    fun selectAllByDate(calendar: Calendar): List<AppData>
    fun insert(entity: AppEntity): Long
    fun update(data: AppData)
    fun delete(data: AppData)
}