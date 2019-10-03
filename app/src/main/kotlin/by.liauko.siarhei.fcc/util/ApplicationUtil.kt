package by.liauko.siarhei.fcc.util

import by.liauko.siarhei.fcc.R
import java.util.Calendar

object ApplicationUtil {
    var type = DataType.LOG
    var appTheme = AppTheme.KITTY
    var dataPeriod = DataPeriod.MONTH
    var periodCalendar = Calendar.getInstance()
}

enum class DataType {
    LOG, FUEL
}

enum class AppTheme(val appId: Int, val dialogId: Int) {
    KITTY(
        R.style.AppDefault,
        R.style.DialogDefault
    ),
    BLUE(
        R.style.AppBlue,
        R.style.DialogBlue
    ),
    RED(
        R.style.AppRed,
        R.style.DialogRed
    ),
    ORANGE(
        R.style.AppOrange,
        R.style.DialogOrange
    ),
    GREEN(
        R.style.AppGreen,
        R.style.DialogGreen
    ),
    DARK(
        R.style.AppDark,
        R.style.DialogDark
    )
}

enum class DataPeriod {
    MONTH, YEAR, ALL
}