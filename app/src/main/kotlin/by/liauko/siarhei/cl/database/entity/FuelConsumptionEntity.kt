package by.liauko.siarhei.cl.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_consumption",
    indices = [Index(value = ["profile_id"], name = "fuel_consumption_profile_id_idx")]
)
data class FuelConsumptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "fuel_consumption") val fuelConsumption: Double,
    val litres: Double,
    val mileage: Int,
    val distance: Double,
    val time: Long,
    @ColumnInfo(name = "profile_id") var profileId: Long
) : AppEntity()
