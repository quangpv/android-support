package android.support.core.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

class DistributionLiveData<T>(def: T? = null) : MediatorLiveData<T>() {
    private var mSource: LiveData<out T>? = null

    init {
        if (def != null) post(def)
    }

    fun connect(source: LiveData<out T>) {
        if (mSource == source) return
        if (mSource != null) removeSource(mSource!!)
        mSource = source
        super.addSource(source) { value = it }
    }

    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        error("Not support")
    }
}