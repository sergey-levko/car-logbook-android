package by.liauko.siarhei.cl.repository.converter

import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.entity.LogData

/**
 * Utility class for converting entity to model and reverse containing data about log records
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
object LogDataConverter {

    /**
     * Converts model to entity
     *
     * @param data model containing information about log records
     *
     * @return database entity containing information about log records
     *
     * @since 4.3
     */
    fun convertToEntity(data: LogData) =
        LogEntity(
            data.id,
            data.title,
            data.text,
            data.mileage,
            data.time,
            data.profileId
        )

    /**
     * Converts entity to model
     *
     * @param entity database entity containing information about log records
     *
     * @return model containing information about log records
     *
     * @since 4.3
     */
    fun convertToData(entity: LogEntity) =
        LogData(
            entity.id,
            entity.time,
            entity.title,
            entity.text,
            entity.mileage,
            entity.profileId
        )
}
