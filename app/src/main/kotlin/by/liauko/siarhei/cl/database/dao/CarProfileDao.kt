package by.liauko.siarhei.cl.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.liauko.siarhei.cl.database.entity.CarProfileEntity

@Dao
interface CarProfileDao {

    @Query("SELECT id, name, body_type, fuel_type, engine_volume FROM car_profile WHERE id = :id")
    fun findById(id: Long): CarProfileEntity

    @Query("SELECT id, name, body_type, fuel_type, engine_volume FROM car_profile")
    fun findAll(): List<CarProfileEntity>

    @Insert
    fun insert(item: CarProfileEntity): Long

    @Insert
    fun insertAll(items: List<CarProfileEntity>)

    @Update
    fun update(item: CarProfileEntity)

    @Delete
    fun delete(item: CarProfileEntity)

    @Query("DELETE FROM car_profile")
    fun deleteAll()
}
