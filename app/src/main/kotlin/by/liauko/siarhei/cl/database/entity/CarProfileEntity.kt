package by.liauko.siarhei.cl.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car_profile")
data class CarProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val name: String,
    @ColumnInfo(name = "body_type") val bodyType: String,
    @ColumnInfo(name = "fuel_type") val fuelType: String,
    @ColumnInfo(name = "engine_volume") val engineVolume: Double?
)