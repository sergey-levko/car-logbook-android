package by.liauko.siarhei.cl.entity

import by.liauko.siarhei.cl.util.CarBodyType
import by.liauko.siarhei.cl.util.CarFuelType

class CarProfileData (
    val id: Long,
    var name: String,
    var bodyType: CarBodyType,
    var fuelType: CarFuelType,
    var engineVolume: Double?
)