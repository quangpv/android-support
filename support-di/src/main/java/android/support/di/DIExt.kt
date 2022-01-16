package android.support.di

import android.app.Application
import androidx.lifecycle.LifecycleOwner


fun module(function: ProvideContext.() -> Unit): Module {
    return Module(dependenceContext, function)
}

fun <T> LifecycleOwner.inject(clazz: Class<T>) = lazy(LazyThreadSafetyMode.NONE) {
    dependenceContext.get(clazz, this)
}

fun <T> inject(clazz: Class<T>) = lazy(LazyThreadSafetyMode.NONE) {
    dependenceContext.get(clazz)
}

fun <T> inject(scopeId: String, clazz: Class<T>) = lazy(LazyThreadSafetyMode.NONE) {
    dependenceContext.get(scopeId, clazz)
}

inline fun <reified T> LifecycleOwner.inject(): Lazy<T> = inject(T::class.java)
inline fun <reified T> inject(): Lazy<T> = inject(T::class.java)

inline fun <reified T> inject(scopeId: String) = inject(scopeId, T::class.java)

val dependenceContext = DependenceContext()

fun Application.dependencies(function: ProvideContext.() -> Unit = {}) {
    dependenceContext.set(this)
    function(dependenceContext)
}