package by.liauko.siarhei.cl.job

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class AbstractAsyncJob : CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun execute() = launch {
        onPreExecute()
        doInBackground()
        onPostExecute()
    }

    fun cancel() {
        job.cancel()
    }

    protected open fun onPreExecute() {}
    protected abstract fun doInBackground()
    protected open fun onPostExecute() {}
}
