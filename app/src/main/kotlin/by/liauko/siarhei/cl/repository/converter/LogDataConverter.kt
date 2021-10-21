package by.liauko.siarhei.cl.repository.converter

import by.liauko.siarhei.cl.database.entity.LogEntity
import by.liauko.siarhei.cl.model.LogDataModel

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
     * @param model model containing information about log records
     *
     * @return database entity containing information about log records
     *
     * @since 4.3
     */
    fun convertToEntity(model: LogDataModel) =
        LogEntity(
            model.id,
            model.title,
            model.text,
            model.mileage,
            model.time,
            model.profileId
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
        LogDataModel(
            entity.id,
            entity.time,
            entity.title,
            entity.text,
            entity.mileage,
            entity.profileId
        )
}
