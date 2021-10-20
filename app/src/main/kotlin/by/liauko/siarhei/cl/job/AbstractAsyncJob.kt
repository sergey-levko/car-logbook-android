package by.liauko.siarhei.cl.job

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Abstract class defining basic methods for asynchronous jobs
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
abstract class AbstractAsyncJob : CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun execute() = launch {
        onPreExecute()
        doInBackground()
        onPostExecute()
    }

    /**
     * Executed before asynchronous task
     *
     * @author Siarhei Liauko
     * @since 4.3
     */
    protected open fun onPreExecute() {}

    /**
     * Must be implemented in child class and contain asynchronous actions
     *
     * @author Siarhei Liauko
     * @since 4.3
     */
    protected abstract suspend fun doInBackground()

    /**
     * Executed after asynchronous task
     *
     * @author Siarhei Liauko
     * @since 4.3
     */
    protected open fun onPostExecute() {}
}
