package by.liauko.siarhei.cl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.repository.DataRepository
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.DataPeriod
import kotlinx.coroutines.launch

/**
 * View model class handling data for [by.liauko.siarhei.cl.activity.fragment.DataFragment]'s UI elements.
 *
 * @param repository [DataRepository] implementation for connecting with database
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
class AppDataViewModel(private val repository: DataRepository<AppData>) : ViewModel() {

    /**
     * Live data containing list of models with information about log records or fuel consumption
     *
     * @since 4.3
     */
    val items: MutableLiveData<ArrayList<AppData>> by lazy {
        MutableLiveData<ArrayList<AppData>>().also {
            mutableListOf<AppData>()
        }
    }

    /**
     * List of items containing previous version of data required for proper updating recycler view
     *
     * @since 4.3
     */
    var oldItems: List<AppData> = emptyList()

    /**
     * Loads data from database according to [ApplicationUtil.dataPeriod] and sorted by item's time in descending order
     *
     * @since 4.3
     */
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

    /**
     * Creates new entity in database, adds it to items list and sorts it by item's time in descending order
     *
     * @param data model containing data about new entity
     *
     * @since 4.3
     */
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

    /**
     * Adds back previously existed item to item list
     *
     * @param index item index pointing to place where item was in list before
     * @param data model containing data about item
     *
     * @since 4.3
     */
    fun restore(index: Int, data: AppData) {
        oldItems = items.value?.toList() ?: emptyList()
        items.value?.add(index, data)
        items.postValue(items.value)
    }

    /**
     * Returns item from list by it's ID
     *
     * @param id the unique identifier of model
     *
     * @return model containing information about log record or fuel consumption with specified ID
     * or null if model with such ID has not found in list
     *
     * @since 4.3
     */
    fun get(id: Long) =
        items.value?.find { it.id == id }

    /**
     * Returns item from list by index
     *
     * @param index index of element which should be returned
     *
     * @return model containing information about log record or fuel consumption stored in list
     * with specified index or null if item with such index has not found in list
     *
     * @since 4.3
     */
    fun get(index: Int) =
        items.value?.get(index)

    /**
     * Returns index of particular item
     *
     * @param item list item which index should be returned
     *
     * @return index of specified item
     *
     * @since 4.3
     */
    fun indexOf(item: AppData) =
        items.value!!.indexOf(item)

    /**
     * Updates information about particular entity in database and updates or removes item related
     * to this entity from list according to new data and [ApplicationUtil.dataPeriod]. If item has
     * been updated list will be sorted by item's time in descending order
     *
     * @param item model containing updated information
     *
     * @since 4.3
     */
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

    /**
     * Removes item with particular index from list
     *
     * @param index index of element which should be removed from list
     *
     * @since 4.3
     */
    fun delete(index: Int) {
        oldItems = items.value?.toList() ?: emptyList()
        (items.value as ArrayList).removeAt(index)
        items.postValue(items.value)
    }

    /**
     * Removes particular entity from database
     *
     * @param item model containing data about entity which should be removed
     *
     * @since 4.3
     */
    fun deleteFromRepo(item: AppData) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
}
