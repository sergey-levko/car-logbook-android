package by.liauko.siarhei.cl.entity

class FuelConsumptionData(
    id: Long?,
    time: Long,
    var fuelConsumption: Double,
    var litres: Double,
    var mileage: Int = 0,
    var distance: Double,
    var profileId: Long
) : AppData(id, time) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FuelConsumptionData

        if (fuelConsumption != other.fuelConsumption) return false
        if (litres != other.litres) return false
        if (mileage != other.mileage) return false
        if (distance != other.distance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fuelConsumption.hashCode()
        result = 31 * result + litres.hashCode()
        result = 31 * result + mileage
        result = 31 * result + distance.hashCode()
        return result
    }
}
