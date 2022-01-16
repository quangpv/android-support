package android.support.di

abstract class LookupContext {
    abstract fun <T> getOrNull(clazz: Class<T>): T?

    abstract fun <T> getOrNull(scopeId: String, clazz: Class<T>): T?

    fun <T> get(clazz: Class<T>): T {
        return getOrNull(clazz) ?: error("Not found bean ${clazz.simpleName}")
    }

    fun <T> get(scopeId: String, clazz: Class<T>): T {
        return getOrNull(scopeId, clazz) ?: error("Not found bean ${clazz.simpleName}")
    }

    inline fun <reified T> get(scopeId: String): T {
        return get(scopeId, T::class.java)
    }

    inline fun <reified T> get(): T {
        return get(T::class.java)
    }
}