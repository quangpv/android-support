package android.support.core.extensions

import android.support.core.event.Event
import android.support.core.flow.DistributionFlow
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn

interface SubscriberExt {
    fun <T> LiveData<T>.bind(observer: Observer<in T>)
    fun <T, V> LiveData<T>.bind(observer: Observer<in V>, map: T.() -> V) {
        bind { observer.onChanged(map(it)) }
    }

    fun <T> Flow<T>.bind(observer: Observer<in T>)
    fun <T, V> Flow<T>.bind(observer: Observer<in V>, map: T.() -> V) {
        bind { observer.onChanged(map(it)) }
    }

    fun <T> Event<T>.bind(observer: Observer<T>)
    fun <T, V> Event<T>.bind(observer: Observer<in V>, map: T.() -> V) {
        bind { observer.onChanged(map(it)) }
    }
}

interface LifecycleSubscriberExt : SubscriberExt, LifecycleOwner {
    private val bindOwner: LifecycleOwner
        get() = if (this is Fragment) viewLifecycleOwner
        else this

    override fun <T> Flow<T>.bind(observer: Observer<in T>) {
        val self = this
        bindOwner.lifecycleScope.launchWhenStarted {
            val flow = if (self is StateFlow
                || self is DistributionFlow
            ) self else self
                .stateIn(this)
            flow.collect {
                observer.onChanged(it)
            }
        }
    }

    override fun <T> LiveData<T>.bind(observer: Observer<in T>) {
        observe(bindOwner, observer)
    }

    override fun <T> Event<T>.bind(observer: Observer<T>) {
        observe(bindOwner, observer)
    }
}