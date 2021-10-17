package by.liauko.siarhei.cl.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.liauko.siarhei.cl.repository.CarProfileRepository
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository
import by.liauko.siarhei.cl.repository.LogRepository

class CarProfileViewModelFactory(
    private val repository: CarProfileRepository,
    private val logRepository: LogRepository,
    private val fuelConsumptionRepository: FuelConsumptionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(
            CarProfileRepository::class.java,
            LogRepository::class.java,
            FuelConsumptionRepository::class.java
        ).newInstance(repository, logRepository, fuelConsumptionRepository)
}
