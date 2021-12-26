package by.liauko.siarhei.cl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.liauko.siarhei.cl.model.DataModel
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
class AppDataViewModel(private val repository: DataRepository<DataModel>) : ViewModel() {

    private val list = arrayListOf<DataModel>()

    /**
     * Live data containing list of models with information about log records or fuel consumption
     *
     * @since 4.3
     */
    val items: MutableLiveData<List<DataModel>> by lazy {
        MutableLiveData<List<DataModel>>(list)
    }

    /**
     * List of items containing previous version of data required for proper updating recycler view
     *
     * @since 4.3
     */
    var oldItems = emptyList<DataModel>()

    /**
     * Loads data from database according to [ApplicationUtil.dataPeriod] and sorted by item's time in descending order
     *
     * @since 4.3
     */
    suspend fun loadItems() {
        list.clear()
        when (ApplicationUtil.dataPeriod) {
            DataPeriod.ALL -> findAllByProfileId()
            else -> findAllByProfileIdAndPeriod()
        }
    }

    private suspend fun findAllByProfileId() {
        list.addAll(repository.selectAllByProfileId(ApplicationUtil.profileId).sortedByDescending { it.time })
    }

    private suspend fun findAllByProfileIdAndPeriod() {
        list.addAll(repository.selectAllByProfileIdAndPeriod(ApplicationUtil.profileId).sortedByDescending { it.time })
    }

    /**
     * Creates new entity in database and if created item has time value in time bounds according to
     * [ApplicationUtil.dataPeriod] adds it to items list and sorts it by item's time in descending order
     *
     * @param item model containing data about new entity
     *
     * @since 4.3
     */
    fun add(item: DataModel) {
        viewModelScope.launch {
            val id = repository.insert(item)
            val timeBounds = ApplicationUtil.prepareDateRange()
            if (id != -1L
                && ApplicationUtil.dataPeriod != DataPeriod.ALL
                && item.time >= timeBounds.first
                && item.time <= timeBounds.second
            ) {
                oldItems = list.toList()
                item.id = id
                list.add(item)
                list.sortByDescending { it.time }
                items.postValue(list)
            }
        }
    }

    /**
     * Adds back previously existed item to item list
     *
     * @param index item index pointing to place where item was in list before
     * @param item model containing data about item
     *
     * @since 4.3
     */
    fun restore(index: Int, item: DataModel) {
        oldItems = list.toList()
        list.add(index, item)
        items.postValue(list)
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
    fun get(id: Long) = list.find { it.id == id }

    /**
     * Returns item from list by index
     *
     * @param index index of element which should be returned
     *
     * @return model containing information about log record or fuel consumption stored in list
     * with specified index
     *
     * @since 4.3
     */
    fun get(index: Int) = list[index]

    /**
     * Returns index of particular item
     *
     * @param item list item which index should be returned
     *
     * @return index of specified item
     *
     * @since 4.3
     */
    fun indexOf(item: DataModel) = list.indexOf(item)

    /**
     * Updates information about particular entity in database and updates or removes item related
     * to this entity from list according to new data and [ApplicationUtil.dataPeriod]. If item has
     * been updated list will be sorted by item's time in descending order
     *
     * @param item model containing updated information
     *
     * @since 4.3
     */
    fun update(item: DataModel) {
        oldItems = list.toList()
        viewModelScope.launch {
            repository.update(item)
            val timeBounds = ApplicationUtil.prepareDateRange()
            if (ApplicationUtil.dataPeriod != DataPeriod.ALL
                && item.time >= timeBounds.first
                && item.time <= timeBounds.second
            ) {
                list.sortByDescending { it.time }
            } else {
                list.remove(item)
            }
            items.postValue(list)
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
        oldItems = list.toList()
        list.removeAt(index)
        items.postValue(list)
    }

    /**
     * Removes particular entity from database
     *
     * @param item model containing data about entity which should be removed
     *
     * @since 4.3
     */
    fun deleteFromRepo(item: DataModel) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
}
