package android.support.core.extensions

fun Boolean?.safe(def: Boolean = false): Boolean {
    return this ?: def
}

fun Int?.safe(def: Int = 0): Int {
    return this ?: def
}


infix fun Boolean.throws(message: String) {
    if (this) throw IllegalArgumentException(message)
}

fun <T : Any> block(any: T?, block: T.() -> Unit) {
    if (any != null) block(any)
}

operator fun <T> T.plus(items: List<T>): List<T> {
    if (items is ArrayList) return items.also { it.add(0, this) }
    return items.toMutableList().also { it.add(0, this) }
}

fun <E, K> List<E>?.toMap(keyOf: E.() -> K): HashMap<K, E> {
    val hashMap = hashMapOf<K, E>()
    if (this == null) return hashMap
    forEach {
        hashMap[keyOf(it)] = it
    }
    return hashMap
}

interface TryBlock<T> {

    infix fun returns(function: (Throwable) -> T): T
    infix fun answers(function: (Throwable) -> Unit)
    infix fun throws(exception: (Throwable) -> RuntimeException): T

    class Success<T>(private val result: T) : TryBlock<T> {
        override fun returns(function: (Throwable) -> T): T {
            return result
        }

        override fun answers(function: (Throwable) -> Unit) {
        }

        override fun throws(exception: (Throwable) -> RuntimeException): T {
            return result
        }
    }

    class Error<T>(private val e: Throwable) : TryBlock<T> {
        override fun returns(function: (Throwable) -> T): T {
            return function(e)
        }

        override fun answers(function: (Throwable) -> Unit) {
            function(e)
        }

        override fun throws(exception: (Throwable) -> RuntimeException): T {
            throw exception(e)
        }
    }
}

fun <T> tryWith(function: () -> T): TryBlock<T> {
    return try {
        TryBlock.Success(function())
    } catch (e: Throwable) {
        TryBlock.Error(e)
    }
}