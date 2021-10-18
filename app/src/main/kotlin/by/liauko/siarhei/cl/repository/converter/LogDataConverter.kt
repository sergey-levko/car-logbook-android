package by.liauko.siarhei.cl.repository.converter

import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.entity.LogData

object LogDataConverter {

    fun convertToEntity(data: LogData) =
        LogEntity(
            data.id,
            data.title,
            data.text,
            data.mileage,
            data.time,
            data.profileId
        )

    fun convertToData(entity: LogEntity) =
        LogData(
            entity.id!!,
            entity.time,
            entity.title,
            entity.text,
            entity.mileage,
            entity.profileId
        )
}
