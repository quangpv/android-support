package android.support.di

interface InstanceFactory<T> {
    fun create(lookupContext: LookupContext): T
}

class ProvideInstanceFactory<T>(
    private val function: LookupContext.() -> T
) : InstanceFactory<T> {
    override fun create(lookupContext: LookupContext): T {
        return function(lookupContext)
    }
}

class ReflectNewInstanceFactory<T>(
    private val clazz: Class<out T>
) : InstanceFactory<T> {

    @Suppress("unchecked_cast")
    override fun create(lookupContext: LookupContext): T {
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
            throw e
        }
    }
}