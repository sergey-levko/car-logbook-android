package by.liauko.siarhei.cl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.repository.DataRepository
import kotlinx.coroutines.launch

class AppDataViewModel(private val repository: DataRepository) : ViewModel() {

    val items: MutableLiveData<List<AppData>> by lazy {
        MutableLiveData<List<AppData>>().also {
            mutableListOf<AppData>()
        }
    }

    var oldItems: List<AppData> = emptyList()

    fun findAllByProfileId(profileId: Long) {
        viewModelScope.launch {
            items.postValue(
                ArrayList(
                    repository.selectAllByProfileId(profileId).sortedByDescending { it.time })
            )
        }
    }

    fun findAllByProfileIdAndPeriod(profileId: Long) {
        viewModelScope.launch {
            items.postValue(
                ArrayList(
                    repository.selectAllByProfileIdAndPeriod(profileId)
                        .sortedByDescending { it.time })
            )
        }
    }

    fun add(data: AppData) {
        oldItems = items.value ?: emptyList()
        viewModelScope.launch {
            val id = repository.insert(data)
            if (id != -1L) {
                data.id = id
                (items.value as ArrayList).add(data)
                items.postValue(items.value!!.sortedByDescending { it.time })
            }
        }
    }

    fun restore(index: Int, data: AppData) {
        oldItems = items.value ?: emptyList()
        (items.value as ArrayList).add(index, data)
        items.postValue(items.value)
    }

    fun get(id: Long) =
        items.value?.find { it.id == id }

    fun get(index: Int) =
        items.value?.get(index)

    fun indexOf(item: AppData) =
        items.value!!.indexOf(item)

    fun update(item: AppData) {
        oldItems = items.value ?: emptyList()
        viewModelScope.launch {
            repository.update(item)
            items.postValue(items.value)
        }
    }

    fun delete(index: Int) {
        oldItems = items.value ?: emptyList()
        (items.value as ArrayList).removeAt(index)
        items.postValue(items.value)
    }

    fun deleteFromRepo(item: AppData) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
}
