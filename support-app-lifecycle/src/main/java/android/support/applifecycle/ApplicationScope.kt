package android.support.applifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ApplicationScope(
    private val timeDelayToClose: Long = 5000
) : CoroutineScope {
    private var mScope: CoroutineScope? = null
    private var mReleaseJob: Job? = null
    private val self get() = this

    override val coroutineContext: CoroutineContext
        get() = mScope?.coroutineContext ?: error("Scope is closed or not initialized yet")

    init {
        ApplicationLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            private fun onStart() {
                mReleaseJob?.cancel()
                mReleaseJob = null
                if (mScope == null) {
                    mScope = MyIOScope()
                }
            }

            private fun onStop() {
                mReleaseJob = self.launch {
                    delay(timeDelayToClose)
                    self.cancel()
                    mScope = null
                }
            }

            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START -> onStart()
                    Lifecycle.Event.ON_STOP -> onStop()
                    else -> {
                    }
                }
            }
        })
    }

    private class MyIOScope : CoroutineScope {
        override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
    }
}