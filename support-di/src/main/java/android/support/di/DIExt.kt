package android.support.di


fun module(function: ProvideContext.() -> Unit): Module {
    return Module(dependenceContext, function)
}

fun <T> inject(clazz: Class<T>) = lazy(LazyThreadSafetyMode.NONE) {
    dependenceContext.get(clazz)
}

inline fun <reified T> inject(): Lazy<T> = inject(T::class.java)

val dependenceContext = DependenceContext()