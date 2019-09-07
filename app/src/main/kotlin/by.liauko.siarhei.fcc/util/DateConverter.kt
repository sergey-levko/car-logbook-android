package by.liauko.siarhei.fcc.util

import java.text.SimpleDateFormat
import java.util.*

object DateConverter {
    fun convert(calendar: Calendar) : String {
        val simpleDateFormat = SimpleDateFormat.getDateInstance()
        return simpleDateFormat.format(calendar.time)
    }
}