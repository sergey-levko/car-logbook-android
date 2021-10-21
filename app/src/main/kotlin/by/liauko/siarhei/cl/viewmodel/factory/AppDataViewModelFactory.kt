package by.liauko.siarhei.cl.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.liauko.siarhei.cl.model.DataModel
import by.liauko.siarhei.cl.repository.DataRepository

class AppDataViewModelFactory(private val repository: DataRepository<out DataModel>) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(DataRepository::class.java).newInstance(repository)
}
