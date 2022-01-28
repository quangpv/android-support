package android.support.di

interface BeanLocator {
    fun <T> lookup(clazz: Class<T>): Bean<T>
}
