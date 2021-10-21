package by.liauko.siarhei.cl.model

open class DataModel (
    var id: Long?,
    var time: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataModel

        if (id != other.id) return false
        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + time.hashCode()
        return result
    }
}
