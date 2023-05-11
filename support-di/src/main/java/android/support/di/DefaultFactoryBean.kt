package android.support.di

internal class DefaultFactoryBean<T>(
    private val instanceFactory: InstanceFactory<T>,
) : Bean<T> {
    override fun getValue(context: LookupContext): T {
        return instanceFactory.create(context)
    }
}