package android.support.persistent.disk

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class TrackingDatabaseAutoCloser(
    private val timeDelayToClose: Long,
    private val debugging: Boolean,
    private val onClose: () -> Unit
) : CoroutineScope {
    private var mCloseJob: Job? = null
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.IO

    private var mCountRef: Int = 0

    fun increaseCount() {
        if (debugging) return
        synchronized(this) {
            mCloseJob?.cancel()
            mCloseJob = null
            mCountRef += 1
        }
    }

    fun decreaseCount(isDBOpen: () -> Boolean) = synchronized(this) {
        if (debugging) return
        if (mCountRef == 0) return
        mCountRef -= 1
        if (mCountRef == 0) {
            if (isDBOpen()) {
                mCloseJob?.cancel()
                mCloseJob = launch {
                    delay(timeDelayToClose)
                    onClose()
                }
            }
        }
    }

}