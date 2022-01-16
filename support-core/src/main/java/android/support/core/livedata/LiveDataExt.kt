package android.support.core.livedata


import android.annotation.SuppressLint
import android.support.core.AbstractComposeData
import android.support.core.ComposeDataImpl
import android.support.core.SuspendComposeDataImpl
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope


fun <T, V> LiveData<T>.map(function: (T?) -> V?): LiveData<V> {
    val next = MediatorLiveData<V>()
    next.addSource(this) {
        next.value = function(it)
    }
    return next
}

fun <T, V> LiveData<T>.coroutineMap(function: suspend CoroutineScope.(T?) -> V?): LiveData<V> {
    val next = CoroutineMediatorLiveData<V>()
    next.addSourceSuspendable(this) {
        next.value = function(it)
    }
    return next
}

fun <T, V> LiveData<T>.mapNotNull(function: (T) -> V?): LiveData<V> {
    val next = MediatorLiveData<V>()
    next.addSource(this) {
        if (it != null) {
            next.value = function(it)
        }
    }
    return next
}

fun <T, V> LiveData<T>.coroutineMapNotNull(function: suspend CoroutineScope.(T) -> V?): LiveData<V> {
    val next = CoroutineMediatorLiveData<V>()
    next.addSourceSuspendable(this) {
        if (it != null) {
            next.value = function(it)
        }
    }
    return next
}

fun <T> LiveData<T>.filter(function: (T) -> Boolean): LiveData<T> {
    return MediatorLiveData<T>().also { next ->
        next.addSource(this) {
            if (function(it)) next.value = it
        }
    }
}

fun <T> LiveData<T>.coroutineFilter(function: suspend CoroutineScope.(T) -> Boolean): LiveData<T> {
    return CoroutineMediatorLiveData<T>().also { next ->
        next.addSourceSuspendable(this) {
            if (function(it)) next.value = it
        }
    }
}

fun <T> LiveData<T>.onEach(function: MutableLiveData<T>.(T) -> Unit): LiveData<T> {
    val next = MediatorLiveData<T>()
    next.addSource(this) {
        next.function(it)
        next.post(it)
    }
    return next
}

fun MutableLiveData<*>.call() {
    this.post(null)
}

fun <T> MutableLiveData<T>.refresh() {
    this.post(value)
}

@SuppressLint("RestrictedApi")
fun <T> MutableLiveData<T>.post(t: T?) {
    if (ArchTaskExecutor.getInstance().isMainThread) value = t
    else postValue(t)
}

fun <T> LiveData<T>.toSingle(): LiveData<T> {
    return SingleLiveEvent<T>().also { next ->
        next.addSource(this) {
            next.value = it
        }
    }
}

fun <A, B, C> combine(
    aLive: LiveData<A>,
    bLive: LiveData<B>,
    function: (A?, B?) -> C?
): LiveData<C> {
    val composeData = ComposeDataImpl(2)
    val liveData = MediatorLiveData<C>()
    composeData.awaitAll {
        liveData.post(function(it.component1(), it.component2()))
    }
    return liveData.also {
        it.addSource(aLive) { a ->
            composeData.put(0, a)
        }
        it.addSource(bLive) { b ->
            composeData.put(1, b)
        }
    }
}

fun <A, B, C> coroutineCombine(
    aLive: LiveData<A>,
    bLive: LiveData<B>,
    function: suspend CoroutineScope.(A?, B?) -> C?
): LiveData<C> {
    val composeData = SuspendComposeDataImpl(2)
    val liveData = CoroutineMediatorLiveData<C>()
    composeData.awaitAll {
        liveData.post(function(it.component1(), it.component2()))
    }
    return liveData.also {
        it.addSourceSuspendable(aLive) { a ->
            composeData.put(0, a)
        }
        it.addSourceSuspendable(bLive) { b ->
            composeData.put(1, b)
        }
    }
}

fun <A, B, C> combineNotNull(
    aLive: LiveData<A>,
    bLive: LiveData<B>,
    function: (A, B) -> C
): LiveData<C> {
    val composeData = ComposeDataImpl(2)
    val liveData = MediatorLiveData<C>()
    composeData.awaitAll {
        liveData.post(function(it.component1(), it.component2()))
    }
    return liveData.also {
        it.addSource(aLive) { a ->
            composeData.put(0, a)
        }
        it.addSource(bLive) { b ->
            composeData.put(1, b)
        }
    }
}

fun <A, B, C> coroutineCombineNotNull(
    aLive: LiveData<A>,
    bLive: LiveData<B>,
    function: suspend CoroutineScope.(A, B) -> C
): LiveData<C> {
    val composeData = SuspendComposeDataImpl(2)
    val liveData = CoroutineMediatorLiveData<C>()
    composeData.awaitAll {
        liveData.post(function(it.component1(), it.component2()))
    }
    return liveData.also {
        it.addSourceSuspendable(aLive) { a ->
            composeData.put(0, a)
        }
        it.addSourceSuspendable(bLive) { b ->
            composeData.put(1, b)
        }
    }
}

@Suppress("unchecked_cast")
fun <A, B, C> mediatorOf(
    aLive: LiveData<A>,
    bLive: LiveData<B>,
    function: (A?, B?) -> C
): LiveData<C> {
    val composeData = ComposeDataImpl(2)
    composeData.anyActivated {
        val first = it.component1<Any?>().takeIf { s -> s != AbstractComposeData.EMPTY }
        val second = it.component2<Any?>().takeIf { s -> s != AbstractComposeData.EMPTY }
        function(first as? A?, second as? B?)
    }
    return MediatorLiveData<C>().also {
        it.addSource(aLive) { a ->
            composeData.put(0, a)
        }
        it.addSource(bLive) { b ->
            composeData.put(1, b)
        }
    }
}

@Suppress("unchecked_cast")
fun <A, B, C> coroutineMediatorOf(
    aLive: LiveData<A>,
    bLive: LiveData<B>,
    function: suspend CoroutineScope.(A?, B?) -> C
): LiveData<C> {
    val composeData = SuspendComposeDataImpl(2)
    composeData.anyActivated {
        val first = it.component1<Any?>().takeIf { s -> s != AbstractComposeData.EMPTY }
        val second = it.component2<Any?>().takeIf { s -> s != AbstractComposeData.EMPTY }
        function(first as? A?, second as? B?)
    }
    return CoroutineMediatorLiveData<C>().also {
        it.addSourceSuspendable(aLive) { a ->
            composeData.put(0, a)

        }
        it.addSourceSuspendable(bLive) { b ->
            composeData.put(1, b)
        }
    }
}

fun <T> LiveData<T>.asMutable(): MutableLiveData<T> {
    return this as? MutableLiveData<T> ?: error("$this is not mutable")
}

fun <T> LiveData<T>.asState(empty: T): MediatorLiveData<T> {
    return MediatorLiveData<T>().also { next ->
        next.post(empty)
        next.addSource(this) {
            next.post(it)
        }
    }
}

fun <T> LiveData<List<T>>.asState(empty: List<T> = emptyList()): MediatorLiveData<List<T>> {
    return MediatorLiveData<List<T>>().also { next ->
        next.post(empty)
        next.addSource(this) {
            next.post(it)
        }
    }
}

fun <T : Any> LiveData<T>.distributeBy(live: DistributionLiveData<T>) {
    live.connect(this)
}

fun <T> LiveData<T>.launchBy(products: LiveData<out Any>) {
    (products as MediatorLiveData).addSource(this) {}
}