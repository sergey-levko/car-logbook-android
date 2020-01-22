package by.liauko.siarhei.fcc.backup

import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.fcc.database.entity.LogEntity

data class BackupEntity(
    val logEntities: List<LogEntity>,
    val fuelConsumptionEntities: List<FuelConsumptionEntity>
)