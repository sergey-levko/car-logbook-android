package by.liauko.siarhei.fcc.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity

@Dao
interface FuelConsumptionDao {

    @Query("SELECT id, fuel_consumption, litres, distance, time FROM fuel_consumption")
    fun findAll(): List<FuelConsumptionEntity>

    @Insert
    fun insert(item: FuelConsumptionEntity): Long

    @Update
    fun update(item: FuelConsumptionEntity)

    @Delete
    fun delete(item: FuelConsumptionEntity)
}