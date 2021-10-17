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

    val profiles: MutableLiveData<List<CarProfileData>> by lazy {
        MutableLiveData<List<CarProfileData>>().also {
            loadCarProfiles()
        }
    }

    var oldItems: List<CarProfileData> = emptyList()

    private fun loadCarProfiles() {
        viewModelScope.launch {
            repository.selectAll().sortedBy { it.name }
        }
    }

    fun add(data: CarProfileData) {
        oldItems = profiles.value ?: emptyList()
        viewModelScope.launch {
            val id = repository.insert(data)
            if (id != -1L) {
                data.id = id
                (profiles.value as ArrayList).add(data)
                profiles.postValue(profiles.value!!.sortedBy { it.name })
            }
        }
    }

    fun get(id: Long) =
        profiles.value?.find { it.id == id }

    fun delete(profile: CarProfileData) {
        oldItems = profiles.value ?: emptyList()
        viewModelScope.launch {
            repository.delete(profile)
            logRepository.deleteAllByProfileId(profile.id!!)
            fuelConsumptionRepository.deleteAllByProfileId(profile.id!!)

            (profiles.value as ArrayList).remove(profile)
            if (profiles.value?.isEmpty() != false) {
                ApplicationUtil.profileId = -1L
                ApplicationUtil.profileName =
                    getApplication<Application>().applicationContext.getString(R.string.app_name)
            } else if (ApplicationUtil.profileId == profile.id!!) {
                ApplicationUtil.profileId = profiles.value!!.first().id!!
                ApplicationUtil.profileName = profiles.value!!.first().name
            }
        }
    }

    fun update(profile: CarProfileData) {
        oldItems = profiles.value ?: emptyList()
        viewModelScope.launch {
            repository.update(profile)
            if (ApplicationUtil.profileId == profile.id!!) {
                ApplicationUtil.profileName = profile.name
            }
        }
    }
}
