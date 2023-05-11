package android.support.di

interface ScopeBeanFactory {
    fun <T> create(shareIn: ShareScope, clazz: Class<T>, factory: InstanceFactory<T>): Bean<T>
    fun <T> create(
        shareIn: Array<out String>,
        clazz: Class<T>,
        factory: InstanceFactory<T>
    ): Bean<T>
}

interface BeanRegister {
    fun <T> registry(clazz: Class<T>, notFoundCallback: BeanNotFoundCallback): Boolean
}