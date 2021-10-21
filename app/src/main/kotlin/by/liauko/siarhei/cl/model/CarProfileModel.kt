package by.liauko.siarhei.cl.model

import by.liauko.siarhei.cl.util.CarBodyType
import by.liauko.siarhei.cl.util.CarFuelType

class CarProfileModel (
    var id: Long?,
    var name: String,
    var bodyType: CarBodyType,
    var fuelType: CarFuelType,
    var engineVolume: Double?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CarProfileModel

        if (id != other.id) return false
        if (name != other.name) return false
        if (bodyType != other.bodyType) return false
        if (fuelType != other.fuelType) return false
        if (engineVolume != other.engineVolume) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + bodyType.hashCode()
        result = 31 * result + fuelType.hashCode()
        result = 31 * result + (engineVolume?.hashCode() ?: 0)
        return result
    }
}
