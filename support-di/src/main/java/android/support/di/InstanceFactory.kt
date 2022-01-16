package android.support.di

import android.util.Log

internal object InstanceFactory {

    @Suppress("unchecked_cast")
    fun <T> create(clazz: Class<T>, lookupContext: LookupContext): T {
        val constructor = clazz.constructors.firstOrNull()
            ?: clazz.declaredConstructors.firstOrNull()
            ?: error("Not found constructor for ${clazz.simpleName}")

        val paramTypes = constructor.genericParameterTypes
        return try {
            val params = paramTypes
                .map { lookupContext.get(it as Class<*>) }
                .toTypedArray()
            constructor.newInstance(*params) as T
        } catch (e: Throwable) {
            Log.e("DependencyContext", "Error lookup for ${clazz.name}")
            throw e
        }
    }
}