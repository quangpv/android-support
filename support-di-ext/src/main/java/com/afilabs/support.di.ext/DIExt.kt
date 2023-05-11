package com.afilabs.support.di.ext

import android.app.Application
import android.content.Context
import android.support.di.ProvideContext
import android.support.di.ScopeOwner
import android.support.di.dependenceContext

fun Application.dependencies(function: ProvideContext.() -> Unit = {}) {
    with(dependenceContext) {
        clear()
        setBeanRegister(ViewModelBeanRegister())
        setScopeBeanFactory(ScopeBeanFactoryImpl())
        single { this@dependencies }
        single<Context> { this@dependencies }
    }
    function(dependenceContext)
}


fun <T> ScopeOwner.inject(clazz: Class<T>) = lazy(LazyThreadSafetyMode.NONE) {
    dependenceContext.get(clazz, this)
}

inline fun <reified T> ScopeOwner.inject(): Lazy<T> = inject(T::class.java)

