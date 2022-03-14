package android.support.di

abstract class ProvideContext {

    abstract fun modules(vararg module: Module)

    abstract fun <T> single(
        override: Boolean = false,
        clazz: Class<T>,
        function: LookupContext.() -> T
    )

    abstract fun <T> factory(
        override: Boolean = false,
        shareIn: ShareScope = ShareScope.None,
        clazz: Class<T>,
        function: LookupContext.() -> T
    )

    abstract fun <T> factory(
        override: Boolean = false,
        shareIn: Array<out String>,
        clazz: Class<T>,
        function: LookupContext.() -> T,
    )

    inline fun <reified T> single(
        override: Boolean = false,
        noinline function: LookupContext.() -> T
    ) {
        return single(override, T::class.java, function)
    }

    inline fun <reified T> factory(
        override: Boolean = false,
        shareIn: ShareScope = ShareScope.None,
        noinline function: LookupContext.() -> T
    ) {
        return factory(override, shareIn, T::class.java, function)
    }

    inline fun <reified T> factory(
        override: Boolean = false,
        shareIn: Array<out String>,
        noinline function: LookupContext.() -> T,
    ) {
        return factory(override, shareIn, T::class.java, function)
    }
}