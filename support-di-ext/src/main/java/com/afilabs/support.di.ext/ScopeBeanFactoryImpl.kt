package com.afilabs.support.di.ext

import android.support.di.Bean
import android.support.di.InstanceFactory
import android.support.di.ScopeBeanFactory
import android.support.di.ShareScope

class ScopeBeanFactoryImpl : ScopeBeanFactory {
    override fun <T> create(
        shareIn: ShareScope,
        clazz: Class<T>,
        factory: InstanceFactory<T>
    ): Bean<T> {
        return when (shareIn) {
            ShareScope.Activity -> ActivityScopeBean(clazz, factory)
            ShareScope.Fragment -> FragmentScopeBean(clazz, factory)
            else -> FragmentOrActivityScopeBean(clazz, factory)
        }
    }

    override fun <T> create(
        shareIn: Array<out String>,
        clazz: Class<T>,
        factory: InstanceFactory<T>
    ): Bean<T> {
        return NamedScopeBean(shareIn, clazz, factory)
    }

}