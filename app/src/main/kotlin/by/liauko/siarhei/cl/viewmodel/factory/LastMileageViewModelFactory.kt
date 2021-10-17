package by.liauko.siarhei.cl.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository

class LastMileageViewModelFactory(private val repository: FuelConsumptionRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(FuelConsumptionRepository::class.java).newInstance(repository)
}
