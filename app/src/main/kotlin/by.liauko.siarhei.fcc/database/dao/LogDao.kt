package by.liauko.siarhei.fcc.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.liauko.siarhei.fcc.database.entity.LogEntity

@Dao
interface LogDao {

    @Query("SELECT id, title, text, mileage, time FROM log")
    fun findAll(): List<LogEntity>

    @Insert
    fun insert(logItem: LogEntity): Long

    @Update
    fun update(logItem: LogEntity)

    @Delete
    fun delete(logItem: LogEntity)
}