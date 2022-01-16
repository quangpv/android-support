package android.support.di

class Module(
    private val context: DependenceContext,
    private val provide: (ProvideContext) -> Unit
) : ProvideContext() {
    private var mModules: Array<out Module>? = null

    override fun modules(vararg module: Module) {
        mModules = module
    }

    override fun <T> single(override: Boolean, clazz: Class<T>, function: LookupContext.() -> T) {
        context.single(override, clazz, function)
    }

    override fun <T> factory(
        override: Boolean,
        shareIn: ShareScope,
        clazz: Class<T>,
        function: LookupContext.() -> T
    ) {
        context.factory(override, shareIn, clazz, function)
    }

    override fun <T> scope(scopeId: String, clazz: Class<T>, function: LookupContext.() -> T) {
        context.scope(scopeId, clazz, function)
    }

    fun provide() {
        mModules?.forEach { it.provide() }
        provide(context)
    }
}