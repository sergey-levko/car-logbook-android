package by.liauko.siarhei.cl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository
import by.liauko.siarhei.cl.util.ApplicationUtil
import kotlinx.coroutines.launch

/**
 * View model class handling data for [by.liauko.siarhei.cl.activity.FuelDataActivity]'s UI elements.
 *
 * @param repository [FuelConsumptionRepository] for maintain data about fuel consumption in database
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
class FuelDataViewModel(private val repository: FuelConsumptionRepository)
    : ViewModel() {

    /**
     * Live data containing last mileage for particular car profile
     *
     * @since 4.3
     */
    private var lastMileage = 0

    var mileage = MutableLiveData(0)
    var distance = MutableLiveData(0.0)

    /**
     * Load information about last mileage for car profile which ID specified in [ApplicationUtil.profileId]
     *
     * @since 4.3
     */

    fun loadLastMileage() {
        viewModelScope.launch {
            lastMileage = repository.selectLastMileage(ApplicationUtil.profileId)
        }
    }

    fun handleMileageChange(mileageValue: String) {
        if (mileageValue.isNotBlank()) {
            distance.postValue(
                if (mileageValue.toInt() - lastMileage > 0) mileageValue.toDouble() - lastMileage
                else 0.0
            )
        }
    }

    fun handleDistanceChange(distanceValue: String) {
        if (distanceValue.isNotBlank()) {
            mileage.postValue(lastMileage + distanceValue.toInt())
        }
    }
}
