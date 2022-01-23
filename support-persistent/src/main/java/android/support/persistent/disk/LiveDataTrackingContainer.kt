package android.support.persistent.disk

import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

class LiveDataTrackingContainer(
    private val debug: Boolean = false
) : CoroutineScope, SqliteTransactionCallback {
    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        if (debug) e.printStackTrace()
    }

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + exceptionHandler + Dispatchers.IO

    private val mTableCache = hashMapOf<String, TableLiveData>()

    private fun notifyTableUpdate(tableName: String) {
        val table = mTableCache[tableName] ?: return
        table.updateChange()
    }

    fun <T> create(
        tableName: String,
        call: suspend () -> T?,
    ): LiveData<T> {
        val table = getTable(tableName)
        return object : LiveData<T>(), TrackingLiveData {
            private var mRefreshJob: Job? = null
            var version = -1
            val shouldRefresh: Boolean
                get() = table.version != version

            override fun onActive() {
                table.add(this)
                if (shouldRefresh) {
                    refreshData()
                }
            }

            override fun onInactive() {
                table.remove(this)
                mRefreshJob = null
            }

            override fun refreshData() {
                if (!hasActiveObservers()) return
                version = table.version
                mRefreshJob?.cancel()
                mRefreshJob = launch {
                    postValue(call())
                }
            }
        }
    }

    private fun getTable(tableName: String): TableLiveData {
        var table = mTableCache[tableName]
        if (table == null) {
            table = TableLiveData()
            mTableCache[tableName] = table
        }
        return table
    }

    private class TableLiveData {
        private var mVersion: Int = 0
        val liveDataList = ConcurrentLinkedQueue<TrackingLiveData>()

        val version get() = synchronized(this) { mVersion }

        fun add(it: TrackingLiveData) {
            liveDataList.add(it)
        }

        fun remove(liveData: TrackingLiveData) {
            liveDataList.remove(liveData)
        }

        fun updateChange() {
            synchronized(this) { mVersion += 1 }
            liveDataList.forEach {
                it.refreshData()
            }
        }
    }

    private interface TrackingLiveData {
        fun refreshData()
    }

    override fun onCommitSucceed(tableName: String) {
        notifyTableUpdate(tableName)
    }

}