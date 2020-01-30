package by.liauko.siarhei.fcc.util

import android.app.AlertDialog
import android.content.Context
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.activity.dialog.ProgressDialog
import java.util.Calendar

object ApplicationUtil {

    var type = DataType.LOG
    var appTheme = AppTheme.KITTY
    var dataPeriod = DataPeriod.MONTH
    var periodCalendar: Calendar = Calendar.getInstance()

    fun createProgressDialog(context: Context, messageId: Int)
            = ProgressDialog(context, context.getString(messageId))

    fun createAlertDialog(context: Context, titleId: Int, messageId: Int): AlertDialog
            = AlertDialog.Builder(context)
        .setTitle(titleId)
        .setMessage(messageId)
        .setNeutralButton(
            context.getString(R.string.dialog_backup_alert_ok_button)
        ) { dialog, _ -> dialog.dismiss() }
        .create()
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