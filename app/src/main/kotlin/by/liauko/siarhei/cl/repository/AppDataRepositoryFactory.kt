package by.liauko.siarhei.cl.repository

import android.content.Context
import by.liauko.siarhei.cl.entity.AppData
import by.liauko.siarhei.cl.util.DataType

/**
 * Factory class creating [DataRepository] according to specified type
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
object AppDataRepositoryFactory {

    /**
     * Returns repository according to specified type
     *
     * @param context application context
     * @param type type of data
     *
     * @return [DataRepository] instance related to specified data type
     *
     * @since 4.3
     */
    fun getRepository(context: Context, type: DataType): DataRepository<out AppData> {
        return when (type) {
            DataType.LOG -> LogRepository(context)
            DataType.FUEL -> FuelConsumptionRepository(context)
        }
    }
}
