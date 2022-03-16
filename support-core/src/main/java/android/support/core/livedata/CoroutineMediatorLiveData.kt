package android.support.core.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class CoroutineMediatorLiveData<T>(private val timeout: Long = 5000) : MediatorLiveData<T>() {
    private var mScope: CoroutineScope? = null
    protected val scope get() = mScope ?: error("My Scope not initialized yet!")

    private fun getOrCreateScope(): CoroutineScope {
        if (mScope == null) {
            synchronized(this) {
                if (mScope == null) mScope = MyScope()
            }
        }
        return mScope!!
    }

    override fun onInactive() {
        super.onInactive()
        mScope?.launch {
            delay(timeout)
            if (!hasActiveObservers()) {
                mScope?.cancel()
                mScope = null
            }
        }
    }

    fun <R> addSourceSuspendable(
        source: LiveData<R>,
        function: suspend CoroutineScope. (R) -> Unit,
    ) {
        super.addSource(source) {
            getOrCreateScope().launch { function(it) }
        }
    }

    private class MyScope : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            SupervisorJob() + Dispatchers.Main.immediate
    }
}