package by.liauko.siarhei.cl.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_consumption")
data class FuelConsumptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "fuel_consumption") val fuelConsumption: Double,
    val litres: Double,
    val distance: Double,
    val time: Long,
    @ColumnInfo(name = "profile_id") val profileId: Long?
) : AppEntity()