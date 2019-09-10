package by.liauko.siarhei.fcc.entity

data class FuelConsumptionData(val id: Long,
                               var fuelConsumption: Double,
                               var litres: Double,
                               var distance: Double,
                               var time: Long)