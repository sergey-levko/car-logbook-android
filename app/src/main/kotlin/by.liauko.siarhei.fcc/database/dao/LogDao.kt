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

    @Query("SELECT id, title, text, mileage, time FROM log WHERE time BETWEEN :startTime AND :endTime")
    fun findAllByDate(startTime: Long, endTime: Long): List<LogEntity>

    @Insert
    fun insert(item: LogEntity): Long

    @Update
    fun update(item: LogEntity)

    @Delete
    fun delete(item: LogEntity)
}