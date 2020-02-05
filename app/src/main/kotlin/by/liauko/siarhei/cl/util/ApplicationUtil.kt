package by.liauko.siarhei.cl.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.dialog.ProgressDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

object ApplicationUtil {

    var type = DataType.LOG
    var dataPeriod = DataPeriod.MONTH
    var periodCalendar: Calendar = Calendar.getInstance()

    fun createProgressDialog(context: Context, messageId: Int)
            = ProgressDialog(context, context.getString(messageId))

    fun createAlertDialog(context: Context, titleId: Int, messageId: Int): AlertDialog
            = MaterialAlertDialogBuilder(context)
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

enum class DataPeriod {
    MONTH, YEAR, ALL
}