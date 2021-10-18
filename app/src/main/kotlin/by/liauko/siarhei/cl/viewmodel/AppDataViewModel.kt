package by.liauko.siarhei.cl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.repository.DataRepository
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.DataPeriod
import kotlinx.coroutines.launch

class AppDataViewModel(private val repository: DataRepository<AppData>) : ViewModel() {

    val items: MutableLiveData<ArrayList<AppData>> by lazy {
        MutableLiveData<ArrayList<AppData>>().also {
            mutableListOf<AppData>()
        }
    }

    var oldItems: List<AppData> = emptyList()

    fun loadItems() {
        viewModelScope.launch {
            when (ApplicationUtil.dataPeriod) {
                DataPeriod.ALL -> findAllByProfileId()
                else -> findAllByProfileIdAndPeriod()
            }
        }
    }

    private suspend fun findAllByProfileId() {
        items.postValue(
            ArrayList(
                repository.selectAllByProfileId(ApplicationUtil.profileId)
                    .sortedByDescending { it.time })
        )
    }

    private suspend fun findAllByProfileIdAndPeriod() {
        items.postValue(
            ArrayList(
                repository.selectAllByProfileIdAndPeriod(ApplicationUtil.profileId)
                    .sortedByDescending { it.time })
        )
    }

    fun add(data: AppData) {
        oldItems = items.value?.toList() ?: emptyList()
        viewModelScope.launch {
            val id = repository.insert(data)
            if (id != -1L) {
                data.id = id
                items.value?.add(data)
                items.postValue(ArrayList(items.value!!.sortedByDescending { it.time }))
            }
        }
    }

    fun restore(index: Int, data: AppData) {
        oldItems = items.value?.toList() ?: emptyList()
        items.value?.add(index, data)
        items.postValue(items.value)
    }

    fun get(id: Long) =
        items.value?.find { it.id == id }

    fun get(index: Int) =
        items.value?.get(index)

    fun indexOf(item: AppData) =
        items.value!!.indexOf(item)

    fun update(item: AppData) {
        oldItems = items.value?.toList() ?: emptyList()
        viewModelScope.launch {
            repository.update(item)
            val index = items.value!!.indexOfFirst { it.id == item.id }
            items.value?.removeAt(index)
            val timeBounds = ApplicationUtil.prepareDateRange()
            if (ApplicationUtil.dataPeriod != DataPeriod.ALL
                && item.time >= timeBounds.first
                && item.time <= timeBounds.second
            ) {
                items.value?.add(item)
            }
            items.postValue(ArrayList(items.value!!.sortedByDescending { it.time }))
        }
    }

    fun delete(index: Int) {
        oldItems = items.value?.toList() ?: emptyList()
        (items.value as ArrayList).removeAt(index)
        items.postValue(items.value)
    }

    fun deleteFromRepo(item: AppData) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
}
