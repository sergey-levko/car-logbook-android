package by.liauko.siarhei.cl.entity

class FuelConsumptionData(
    id: Long,
    time: Long,
    var fuelConsumption: Double,
    var litres: Double,
    var distance: Double
) : AppData(id, time)
