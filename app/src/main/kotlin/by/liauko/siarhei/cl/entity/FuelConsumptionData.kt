package by.liauko.siarhei.cl.entity

class FuelConsumptionData(
    id: Long,
    time: Long,
    var fuelConsumption: Double,
    var litres: Double,
    var mileage: Int = 0,
    var distance: Double,
    var profileId: Long
) : AppData(id, time)
