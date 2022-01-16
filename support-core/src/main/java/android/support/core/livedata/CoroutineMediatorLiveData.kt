package android.support.core.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class CoroutineMediatorLiveData<T>(private val timeout: Long = 5000) : MediatorLiveData<T>() {
    private var mScope: CoroutineScope? = null

    override fun onActive() {
        super.onActive()
        mScope = MyScope()
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
        function: suspend CoroutineScope. (R) -> Unit
    ) {
        super.addSource(source) {
            mScope?.launch {
                function(it)
            }
        }
    }

    private class MyScope : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            SupervisorJob() + Dispatchers.Main.immediate
    }
}