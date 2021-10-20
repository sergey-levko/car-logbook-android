package by.liauko.siarhei.cl.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.repository.DataRepository

class AppDataViewModelFactory(private val repository: DataRepository<out AppData>) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(DataRepository::class.java).newInstance(repository)
}
