package android.support.di

internal class SingletonBean<T>(
    private val instanceFactory: InstanceFactory<T>,
) : Bean<T> {
    private var mValue: T? = null

    override fun getValue(context: LookupContext): T {
        if (mValue == null) synchronized(this) {
            if (mValue == null) {
                mValue = instanceFactory.create(
                    (context as? ScopeLookupContext)?.globalContext() ?: context
                )
            }
        }
        return mValue!!
    }

    override fun close() {
        (mValue as? AutoCloseable)?.close()
        mValue = null
    }
}