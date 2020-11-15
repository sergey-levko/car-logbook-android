package by.liauko.siarhei.cl.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "log",
    indices = [Index(value = ["profile_id"], name = "log_profile_id_idx")]
)
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val title: String,
    val text: String?,
    val mileage: Long,
    val time: Long,
    @ColumnInfo(name = "profile_id") var profileId: Long
) : AppEntity()
