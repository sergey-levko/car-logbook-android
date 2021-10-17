package by.liauko.siarhei.cl.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import by.liauko.siarhei.cl.job.ExportToPdfAsyncJob
import by.liauko.siarhei.cl.repository.CarProfileRepository
import by.liauko.siarhei.cl.repository.LogRepository
import by.liauko.siarhei.cl.util.ApplicationUtil
import kotlinx.coroutines.launch

class MainScreenViewModel(
    application: Application,
    private val logRepository: LogRepository,
    private val carProfileRepository: CarProfileRepository
) : AndroidViewModel(application) {

//    private val carProfiles: ArrayList<CarProfileData> = ArrayList()
//    private val logData: ArrayList<LogData> = ArrayList()

    fun exportDataToPdfFile(directoryUri: Uri) {
        viewModelScope.launch {
            val data = logRepository.selectAllByProfileId(ApplicationUtil.profileId).sortedBy { it.time }
            val carData = carProfileRepository.selectById(ApplicationUtil.profileId)
            ExportToPdfAsyncJob(
                getApplication<Application>().applicationContext,
                directoryUri,
                data,
                carData
            ).execute()
        }
    }

//    private val preferences = getApplication<Application>().applicationContext.getSharedPreferences(
//        getApplication<Application>().applicationContext.getString(R.string.shared_preferences_name),
//        Context.MODE_PRIVATE
//    )
//
//    var dataType = DataType.valueOf(
//        preferences.getString(
//            getApplication<Application>().applicationContext.getString(R.string.main_screen_key),
//            "LOG"
//        ) ?: "LOG"
//    )
//    val dataPeriod = DataPeriod.valueOf(
//        preferences.getString(
//            getApplication<Application>().applicationContext.getString(R.string.period_key), "MONTH"
//        ) ?: "MONTH"
//    )
//    val profileId = preferences.getLong(
//        getApplication<Application>().applicationContext.getString(R.string.car_profile_id_key),
//        -1L
//    )
//    val profileName = preferences.getString(
//        getApplication<Application>().applicationContext.getString(R.string.car_profile_name_key),
//        getApplication<Application>().applicationContext.getString(R.string.app_name)
//    ) ?: EMPTY_STRING
//
//    fun updateDataType(type: DataType) {
//        dataType = type
//        ApplicationUtil.type = type
//    }
}
