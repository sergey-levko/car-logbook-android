package by.liauko.siarhei.fcc.entity

class FuelConsumptionData(id: Long,
                          time: Long,
                          var fuelConsumption: Double,
                          var litres: Double,
                          var distance: Double): AppData(id, time)