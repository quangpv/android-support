package android.support.di

interface Bean<T> {
    fun getValue(context: LookupContext): T
    fun close() {}
}
