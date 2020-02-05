package by.liauko.siarhei.cl.util

import java.text.SimpleDateFormat
import java.util.Calendar

object DateConverter {
    fun convert(calendar: Calendar) : String {
        val pattern = "dd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(calendar.time)
    }
}