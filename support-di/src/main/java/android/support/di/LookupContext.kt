package android.support.di

import androidx.lifecycle.LifecycleOwner

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

internal class GlobalLookupContext(locator: BeanLocator) : LookupContext(locator),
    BeanLocator by locator

internal class LifecycleLookupContext(
    private val context: GlobalLookupContext,
    override val owner: LifecycleOwner,
) : LookupContext(context), LifecycleLookup {

    private fun newContext(newOwner: LifecycleOwner): LookupContext {
        return LifecycleLookupContext(context, newOwner)
    }

    fun globalContext() = context

    fun getOrCreate(newOwner: LifecycleOwner): LookupContext {
        return if (newOwner == owner) this
        else newContext(newOwner)
    }
}