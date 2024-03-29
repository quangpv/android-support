package com.afilabs.support.di.ext

import android.app.Activity
import android.support.di.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.getOrPut
import java.util.*

internal abstract class LifecycleBean<T>(
    val instanceFactory: InstanceFactory<T>,
) : Bean<T> {
    final override fun getValue(context: LookupContext): T {
        if (context !is ScopeLookup) {
            return instanceFactory.create(context)
        }
        val owner = context.owner as? ViewModelStoreOwner
            ?: error("${context.owner} should be ViewModelStoreOwner")
        return getValue(context, owner)
    }

    protected abstract fun getValue(context: LookupContext, owner: ViewModelStoreOwner): T

    protected fun getInstanceContainer(owner: ViewModelStoreOwner): InstanceContainer {
        return owner.viewModelStore.getOrPut("android:support:di:share:instance:container") {
            InstanceContainer()
        }
    }

    protected class InstanceContainer : ViewModel() {
        private var mCache = hashMapOf<String, Any>()

        @Suppress("unchecked_cast")
        fun <T> getOrPut(keyClass: Class<*>, def: () -> T): T {
            val key = keyClass.name
            var mValue = mCache[key]
            if (mValue == null) {
                synchronized(this) {
                    if (mCache[key] == null) {
                        mValue = def()
                        mCache[key] = mValue!!
                    }
                }
            }
            return mValue as T
        }

        override fun onCleared() {
            super.onCleared()
            mCache.forEach { (_, v) ->
                if (v is AutoCloseable) {
                    v.close()
                }
            }
            mCache.clear()
        }

        fun <T> put(value: T): T {
            return synchronized(this) {
                var key = UUID.randomUUID().toString()
                while (mCache.containsKey(key)) {
                    key = UUID.randomUUID().toString()
                }
                mCache[key] = value as Any
                value
            }
        }
    }
}

internal class FragmentScopeBean<T>(
    private val keyClass: Class<T>,
    instanceFactory: InstanceFactory<T>,
) : LifecycleBean<T>(instanceFactory) {

    override fun getValue(context: LookupContext, owner: ViewModelStoreOwner): T {
        return when (owner) {
            is Activity -> {
                getInstanceContainer(owner).put(instanceFactory.create(context))
            }
            else -> getInstanceContainer(owner).getOrPut(keyClass) {
                instanceFactory.create(context)
            }
        }
    }
}

internal class ActivityScopeBean<T>(
    private val keyClass: Class<T>,
    instanceFactory: InstanceFactory<T>,
) : LifecycleBean<T>(instanceFactory) {

    override fun getValue(context: LookupContext, owner: ViewModelStoreOwner): T {
        val containerOwner = when (owner) {
            is Activity -> owner
            else -> (owner as Fragment).requireActivity()
        }
        return getInstanceContainer(containerOwner).getOrPut(keyClass) {
            val newContext = (context as ScopeLookupContext)
                .getOrCreate(containerOwner as ScopeOwner)

            instanceFactory.create(newContext)
        }
    }
}

internal class FragmentOrActivityScopeBean<T>(
    private val keyClass: Class<T>,
    instanceFactory: InstanceFactory<T>,
) : LifecycleBean<T>(instanceFactory) {

    override fun getValue(context: LookupContext, owner: ViewModelStoreOwner): T {
        return getInstanceContainer(owner).getOrPut(keyClass) {
            instanceFactory.create(context)
        }
    }
}

internal class NamedScopeBean<T>(
    private val sharedIn: Array<out String>,
    private val keyClass: Class<T>,
    instanceFactory: InstanceFactory<T>,
) : LifecycleBean<T>(instanceFactory) {

    override fun getValue(context: LookupContext, owner: ViewModelStoreOwner): T {
        val scopeOwner = requireScopeOwner(owner)
        return getInstanceContainer(scopeOwner).getOrPut(keyClass) {
            val newContext = (context as ScopeLookupContext)
                .getOrCreate(scopeOwner as ScopeOwner)
            instanceFactory.create(newContext)
        }
    }

    private fun requireScopeOwner(owner: ViewModelStoreOwner): ViewModelStoreOwner {
        var scopeOwner: ViewModelStoreOwner? = owner

        while (scopeOwner != null) {
            val annotation = scopeOwner.javaClass.getAnnotation(NamedScope::class.java)
            if (annotation != null && annotation.name in sharedIn) return scopeOwner
            if (scopeOwner is Activity) break
            if (scopeOwner is Fragment) {
                scopeOwner = scopeOwner.parentFragment ?: scopeOwner.activity
            }
        }
        error("Not found @NamedScope [${sharedIn.joinToString()}]")
    }
}