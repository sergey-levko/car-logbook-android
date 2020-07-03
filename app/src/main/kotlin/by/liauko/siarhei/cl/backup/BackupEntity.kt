package by.liauko.siarhei.cl.backup

import by.liauko.siarhei.cl.database.entity.CarProfileEntity
import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.database.entity.LogEntity

data class BackupEntity(
    val logEntities: List<LogEntity>,
    val fuelConsumptionEntities: List<FuelConsumptionEntity>,
    val carProfileEntities: List<CarProfileEntity>
)