package by.liauko.siarhei.fcc.synchronization

import by.liauko.siarhei.fcc.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.fcc.database.entity.LogEntity

data class BackUpEntity(
    val logEntities: List<LogEntity>,
    val fuelConsumptionEntities: List<FuelConsumptionEntity>
)