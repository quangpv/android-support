package android.support.di

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.getOrPut
import java.util.*

interface Bean<T> {
    fun getValue(context: LookupContext): T
    fun close() {}
}

internal class ApplicationBean(private val application: Application) : Bean<Application> {

    override fun getValue(context: LookupContext): Application {
        return application
    }

    fun isDiff(app: Application): Boolean {
        return application != app
    }
}

internal class SingletonBean<T>(
    private val instanceFactory: InstanceFactory<T>,
) : Bean<T> {
    private var mValue: T? = null

    override fun getValue(context: LookupContext): T {
        if (mValue == null) synchronized(this) {
            if (mValue == null) {
                mValue = instanceFactory.create(
                    (context as? LifecycleLookupContext)?.globalContext() ?: context
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

internal class DefaultFactoryBean<T>(
    private val instanceFactory: InstanceFactory<T>,
) : Bean<T> {
    override fun getValue(context: LookupContext): T {
        return instanceFactory.create(context)
    }
}

internal abstract class LifecycleBean<T>(
    val instanceFactory: InstanceFactory<T>,
) : Bean<T> {
    final override fun getValue(context: LookupContext): T {
        if (context !is LifecycleLookup) {
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
                    if (mValue == null) {
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
        var newContext = context
        val containerOwner = when (owner) {
            is Activity -> owner
            else -> {
                val activity = (owner as Fragment).requireActivity()
                newContext = (context as LifecycleLookupContext).newContext(activity)
                activity
            }
        }
        return getInstanceContainer(containerOwner).getOrPut(keyClass) {
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