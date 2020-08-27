package by.liauko.siarhei.cl.entity

class LogData(
    id: Long,
    time: Long,
    var title: String,
    var text: String,
    var mileage: Long
) : AppData (id, time)
