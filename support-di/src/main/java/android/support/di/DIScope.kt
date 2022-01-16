package android.support.di

interface Scope {
    fun contains(clazz: Class<*>): Boolean
    fun dispose()

    fun <T> factory(clazz: Class<T>, function: LookupContext.() -> T)
    fun <T> lookup(clazz: Class<T>): Bean<T>
}

internal class SimpleScope(private val context: DependenceContext) : Scope {
    private val mBean = hashMapOf<Class<*>, ScopeBean<*>>()

    override fun contains(clazz: Class<*>): Boolean {
        return mBean.containsKey(clazz)
    }

    override fun <T> factory(clazz: Class<T>, function: LookupContext.() -> T) {
        mBean[clazz] = ScopeBean { function(context.globalLookup) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> lookup(clazz: Class<T>): Bean<T> {
        if (!mBean.containsKey(clazz)) factory(clazz) {
            InstanceFactory.create(clazz, this)
        }
        return mBean[clazz] as Bean<T>
    }

    override fun dispose() {
        mBean.values.forEach { it.dispose() }
        mBean.clear()
    }
}