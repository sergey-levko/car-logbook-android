package by.liauko.siarhei.cl.repository.converter

import by.liauko.siarhei.cl.database.entity.FuelConsumptionEntity
import by.liauko.siarhei.cl.model.FuelDataModel

/**
 * Utility class for converting entity to model and reverse containing data about fuel consumption
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
object FuelConsumptionConverter {

    /**
     * Converts model to entity
     *
     * @param model model containing information about fuel consumption
     *
     * @return database entity containing information about fuel consumption
     *
     * @since 4.3
     */
    fun convertToEntity(model: FuelDataModel) =
        FuelConsumptionEntity(
            model.id,
            model.fuelConsumption,
            model.litres,
            model.mileage,
            model.distance,
            model.time,
            model.profileId
        )

    /**
     * Converts entity to model
     *
     * @param entity database entity containing information about fuel consumption
     *
     * @return model containing information about fuel consumption
     *
     * @since 4.3
     */
    fun convertToData(entity: FuelConsumptionEntity) =
        FuelDataModel(
            entity.id,
            entity.time,
            entity.fuelConsumption,
            entity.litres,
            entity.mileage,
            entity.distance,
            entity.profileId
        )
}
