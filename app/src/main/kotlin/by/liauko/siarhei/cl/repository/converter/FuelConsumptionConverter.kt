package by.liauko.siarhei.cl.repository.converter

import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.entity.FuelConsumptionData

object FuelConsumptionConverter {

    fun convertToEntity(data: FuelConsumptionData) =
        FuelConsumptionEntity(
            data.id,
            data.fuelConsumption,
            data.litres,
            data.mileage,
            data.distance,
            data.time,
            data.profileId
        )

    fun convertToData(entity: FuelConsumptionEntity) =
        FuelConsumptionData(
            entity.id!!,
            entity.time,
            entity.fuelConsumption,
            entity.litres,
            entity.mileage,
            entity.distance,
            entity.profileId
        )
}
