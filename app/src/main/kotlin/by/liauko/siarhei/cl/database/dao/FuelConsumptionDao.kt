package by.liauko.siarhei.cl.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity

@Dao
interface FuelConsumptionDao {

    @Query("SELECT id, fuel_consumption, litres, distance, time FROM fuel_consumption")
    fun findAll(): List<FuelConsumptionEntity>

    @Query("SELECT id, fuel_consumption, litres, distance, time FROM fuel_consumption WHERE profileId = :profileId")
    fun findAllByProfileId(profileId: Long): List<FuelConsumptionEntity>

    @Query("SELECT id, fuel_consumption, litres, distance, time FROM fuel_consumption WHERE profileId = :profileId AND time BETWEEN :startTime AND :endTime")
    fun findAllByProfileIdAndDate(profileId: Long, startTime: Long, endTime: Long): List<FuelConsumptionEntity>

    @Insert
    fun insert(item: FuelConsumptionEntity): Long

    @Insert
    fun insertAll(items: List<FuelConsumptionEntity>)

    @Update
    fun update(item: FuelConsumptionEntity)

    @Delete
    fun delete(item: FuelConsumptionEntity)

    @Query("DELETE FROM fuel_consumption")
    fun deleteAll()

    @Query("DELETE FROM fuel_consumption WHERE profileId = :profileId")
    fun deleteAllByProfileId(profileId: Long)
}