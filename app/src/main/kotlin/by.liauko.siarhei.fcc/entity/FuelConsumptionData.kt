package by.liauko.siarhei.fcc.entity

import java.util.*

data class FuelConsumptionData(val id: Long,
                               val fuelConsumption: Double,
                               val litres: Double,
                               val distance: Double,
                               val date: GregorianCalendar)