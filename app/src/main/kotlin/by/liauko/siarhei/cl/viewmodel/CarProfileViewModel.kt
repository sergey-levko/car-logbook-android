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

class CarProfileViewModel(
    application: Application,
    private val repository: CarProfileRepository,
    private val logRepository: LogRepository,
    private val fuelConsumptionRepository: FuelConsumptionRepository
) : AndroidViewModel(application) {

    val profiles: MutableLiveData<ArrayList<CarProfileData>> by lazy {
        MutableLiveData<ArrayList<CarProfileData>>().also {
            mutableListOf<CarProfileData>()
        }
    }

    var oldItems: List<CarProfileData> = emptyList()

    fun loadCarProfiles() {
        viewModelScope.launch {
            profiles.postValue(ArrayList(repository.selectAll().sortedBy { it.name }))
        }
    }

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

    fun get(id: Long) =
        profiles.value?.find { it.id == id }

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
