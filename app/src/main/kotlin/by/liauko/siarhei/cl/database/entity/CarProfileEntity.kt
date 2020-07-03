package by.liauko.siarhei.cl.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car_profile")
data class CarProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val name: String,
    val bodyType: String,
    val fuelType: String,
    val engineVolume: Double?
)