package by.liauko.siarhei.cl.model

class LogDataModel(
    id: Long?,
    time: Long,
    var title: String,
    var text: String?,
    var mileage: Long,
    var profileId: Long
) : DataModel (id, time) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogDataModel

        if (title != other.title) return false
        if (text != other.text) return false
        if (mileage != other.mileage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + mileage.hashCode()
        return result
    }
}
