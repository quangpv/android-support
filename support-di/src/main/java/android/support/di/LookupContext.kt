package android.support.di

abstract class LookupContext(private val locator: BeanLocator) {
    fun <T> getOrNull(clazz: Class<T>): T {
        return locator.lookup(clazz).getValue(this)
    }

    fun <T> get(clazz: Class<T>): T {
        return getOrNull(clazz) ?: error("Not found bean ${clazz.simpleName}")
    }

    inline fun <reified T> get(): T {
        return get(T::class.java)
    }
}

