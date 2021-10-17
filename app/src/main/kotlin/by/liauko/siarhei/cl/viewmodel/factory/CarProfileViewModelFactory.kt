package by.liauko.siarhei.cl.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.liauko.siarhei.cl.repository.CarProfileRepository
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository
import by.liauko.siarhei.cl.repository.LogRepository

class CarProfileViewModelFactory(
    private val application: Application,
    private val repository: CarProfileRepository,
    private val logRepository: LogRepository,
    private val fuelConsumptionRepository: FuelConsumptionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(
            Application::class.java,
            CarProfileRepository::class.java,
            LogRepository::class.java,
            FuelConsumptionRepository::class.java
        ).newInstance(application, repository, logRepository, fuelConsumptionRepository)
}
