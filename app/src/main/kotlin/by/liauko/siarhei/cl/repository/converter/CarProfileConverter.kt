package by.liauko.siarhei.cl.repository.converter

import by.liauko.siarhei.cl.database.entity.CarProfileEntity
import by.liauko.siarhei.cl.entity.CarProfileData
import by.liauko.siarhei.cl.util.CarBodyType
import by.liauko.siarhei.cl.util.CarFuelType

object CarProfileConverter {

    fun convertToEntity(data: CarProfileData) =
        CarProfileEntity(
            data.id,
            data.name,
            data.bodyType.name,
            data.fuelType.name,
            data.engineVolume
        )

    fun convertToData(entity: CarProfileEntity) =
        CarProfileData(
            entity.id,
            entity.name,
            CarBodyType.valueOf(entity.bodyType),
            CarFuelType.valueOf(entity.fuelType),
            entity.engineVolume
        )
}
