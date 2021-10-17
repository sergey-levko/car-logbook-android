package by.liauko.siarhei.cl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository
import by.liauko.siarhei.cl.util.ApplicationUtil
import kotlinx.coroutines.launch

class LastMileageViewModel(private val repository: FuelConsumptionRepository)
    : ViewModel() {

    var lastMileage = 0

    fun findLastMileage(): Int {
        viewModelScope.launch {
            lastMileage = repository.selectLastMileage(ApplicationUtil.profileId)
        }

        return lastMileage
    }
}
