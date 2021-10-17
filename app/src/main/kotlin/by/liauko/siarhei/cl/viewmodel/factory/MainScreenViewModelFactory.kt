package by.liauko.siarhei.cl.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.liauko.siarhei.cl.repository.CarProfileRepository
import by.liauko.siarhei.cl.repository.DataRepository
import by.liauko.siarhei.cl.repository.LogRepository

class MainScreenViewModelFactory(
    private val logRepository: LogRepository,
    private val carProfileRepository: CarProfileRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(DataRepository::class.java).newInstance(logRepository, carProfileRepository)
}
