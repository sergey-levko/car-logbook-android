package by.liauko.siarhei.fcc.entity

class LogData(id: Long,
              time: Long,
              var title: String,
              var text: String,
              var mileage: Long): Data (id, time)