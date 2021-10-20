package by.liauko.siarhei.cl.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.entity.CarProfileData
import by.liauko.siarhei.cl.repository.CarProfileRepository
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository
import by.liauko.siarhei.cl.repository.LogRepository
import by.liauko.siarhei.cl.util.ApplicationUtil
import kotlinx.coroutines.launch

/**
 * View model class handling data for [by.liauko.siarhei.cl.activity.CarProfilesActivity]'s UI elements.
 *
 * @param application [Application] instance required for receiving application context
 * @param repository [CarProfileRepository] for maintain data about car profiles in database
 * @param logRepository [LogRepository] for maintain data about log records in database
 * @param fuelConsumptionRepository [FuelConsumptionRepository] for maintain data about fuel consumption in database
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
class CarProfileViewModel(
    application: Application,
    private val repository: CarProfileRepository,
    private val logRepository: LogRepository,
    private val fuelConsumptionRepository: FuelConsumptionRepository
) : AndroidViewModel(application) {

    /**
     * Live data containing list of models with information about car profiles
     *
     * @since 4.3
     */
    val profiles: MutableLiveData<ArrayList<CarProfileData>> by lazy {
        MutableLiveData<ArrayList<CarProfileData>>().also {
            mutableListOf<CarProfileData>()
        }
    }

    /**
     * List of items containing previous version of data required for proper updating recycler view
     *
     * @since 4.3
     */
    var oldItems: List<CarProfileData> = emptyList()

    /**
     * Loads data from database sorted by item's name
     *
     * @since 4.3
     */
    fun loadCarProfiles() {
        viewModelScope.launch {
            profiles.postValue(ArrayList(repository.selectAll().sortedBy { it.name }))
        }
    }

    /**
     * Creates new entity in database, adds it to items list and sorts it by item's name
     *
     * @param data model containing data about new car profile entity
     *
     * @since 4.3
     */
    fun add(data: CarProfileData) {
        oldItems = profiles.value?.toList() ?: emptyList()
        viewModelScope.launch {
            val id = repository.insert(data)
            if (id != -1L) {
                data.id = id
                profiles.value?.add(data)
                profiles.postValue(ArrayList(profiles.value!!.sortedBy { it.name }))
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
    fun get(id: Long) =
        profiles.value?.find { it.id == id }

    /**
     * Removes item with particular ID from list and update application state properties if necessary
     *
     * @param id the unique identifier of car profile model which should be removed from list
     *
     * @since 4.3
     */
    fun delete(id: Long) {
        oldItems = profiles.value?.toList() ?: emptyList()
        viewModelScope.launch {
            val profile = profiles.value?.find { it.id == id }!!
            repository.delete(profile)
            logRepository.deleteAllByProfileId(profile.id!!)
            fuelConsumptionRepository.deleteAllByProfileId(profile.id!!)

            profiles.value?.remove(profile)
            if (profiles.value?.isEmpty() != false) {
                ApplicationUtil.profileId = -1L
                ApplicationUtil.profileName =
                    getApplication<Application>().applicationContext.getString(R.string.app_name)
            } else if (ApplicationUtil.profileId == profile.id!!) {
                ApplicationUtil.profileId = profiles.value!!.minByOrNull { it.name }!!.id!!
                ApplicationUtil.profileName = profiles.value!!.minByOrNull { it.name }!!.name
            }
            profiles.postValue(profiles.value)
        }
    }

    /**
     * Updates information about particular car profile entity in database, updates item related
     * to this entity in list according to new data and sorts list by item's name
     *
     * @param profile car profile model containing updated information
     *
     * @since 4.3
     */
    fun update(profile: CarProfileData) {
        oldItems = profiles.value?.toList() ?: emptyList()
        viewModelScope.launch {
            repository.update(profile)
            val index = profiles.value?.indexOfFirst { it.id == profile.id }!!
            profiles.value?.removeAt(index)
            profiles.value?.add(profile)
            if (ApplicationUtil.profileId == profile.id!!) {
                ApplicationUtil.profileName = profile.name
            }

            profiles.postValue(ArrayList(profiles.value!!.sortedBy { it.name }))
        }
    }
}
