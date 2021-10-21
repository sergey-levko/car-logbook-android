package by.liauko.siarhei.cl.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.model.CarProfileModel
import by.liauko.siarhei.cl.repository.CarProfileRepository
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository
import by.liauko.siarhei.cl.repository.LogRepository
import by.liauko.siarhei.cl.util.ApplicationUtil
import kotlinx.coroutines.launch

/**
 * View model class handling data for [by.liauko.siarhei.cl.activity.CarProfilesActivity]'s UI elements.
 *
 * @param application [Application] instance required for receiving application context
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
class CarProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CarProfileRepository(getApplication())
    private val logRepository = LogRepository(getApplication())
    private val fuelConsumptionRepository = FuelConsumptionRepository(getApplication())

    private val list = arrayListOf<CarProfileModel>()

    /**
     * Live data containing list of models with information about car profiles
     *
     * @since 4.3
     */
    val profiles: MutableLiveData<List<CarProfileModel>> by lazy {
        MutableLiveData<List<CarProfileModel>>(list)
    }

    /**
     * List of items containing previous version of data required for proper updating recycler view
     *
     * @since 4.3
     */
    var oldItems = emptyList<CarProfileModel>()

    /**
     * Loads data from database sorted by item's name
     *
     * @since 4.3
     */
    suspend fun loadCarProfiles() {
        list.clear()
        list.addAll(repository.selectAll().sortedBy { it.name })
    }

    /**
     * Creates new entity in database, adds it to items list and sorts it by item's name
     *
     * @param profile model containing data about new car profile entity
     *
     * @since 4.3
     */
    fun add(profile: CarProfileModel) {
        viewModelScope.launch {
            val id = repository.insert(profile)
            if (id != -1L) {
                oldItems = list.toList()
                profile.id = id
                list.add(profile)
                list.sortBy { it.name }
                profiles.postValue(list)
            }
        }
    }

    /**
     * Returns item from list by it's ID
     *
     * @param id the unique identifier of car profile model
     *
     * @return model containing information about car profile with specified ID or null if model
     * with such ID has not found in list
     *
     * @since 4.3
     */
    fun get(id: Long) = list.find { it.id == id }

    /**
     * Returns profile from list by index
     *
     * @param index index of element which should be returned
     *
     * @return model containing information about car profile stored in list with specified index
     *
     * @since 4.3
     */
    fun get(index: Int) = list[index]

    /**
     * Returns index of particular profile
     *
     * @param profile list item which index should be returned
     *
     * @return index of specified profile
     *
     * @since 4.3
     */
    fun indexOf(profile: CarProfileModel) = list.indexOf(profile)

    /**
     * Updates information about particular car profile entity in database, updates item related
     * to this entity in list according to new data and sorts list by item's name
     *
     * @param profile car profile model containing updated information
     *
     * @since 4.3
     */
    fun update(profile: CarProfileModel) {
        oldItems = list.toList()
        viewModelScope.launch {
            repository.update(profile)
            list.sortBy { it.name }
            if (ApplicationUtil.profileId == profile.id!!) {
                ApplicationUtil.profileName = profile.name
            }
            profiles.postValue(list)
        }
    }

    /**
     * Removes item with particular ID from list and update application state properties if necessary
     *
     * @param id the unique identifier of car profile model which should be removed from list
     *
     * @since 4.3
     */
    fun delete(id: Long) {
        oldItems = list.toList()
        viewModelScope.launch {
            val profile = list.find { it.id == id }!!
            repository.delete(profile)
            logRepository.deleteAllByProfileId(profile.id!!)
            fuelConsumptionRepository.deleteAllByProfileId(profile.id!!)

            list.remove(profile)
            if (list.isEmpty()) {
                ApplicationUtil.profileId = -1L
                ApplicationUtil.profileName =
                    getApplication<Application>().getString(R.string.app_name)
            }
            profiles.postValue(list)
        }
    }
}
