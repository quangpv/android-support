package android.support.di

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.getOrPut

interface Bean<T> {
    fun getValue(): T
}

internal class SingletonBean<T>(
    private val function: () -> T
) : Bean<T> {
    private var mValue: T? = null

    override fun getValue(): T {
        if (mValue == null) synchronized(this) {
            if (mValue == null) mValue = function()
        }
        return mValue!!
    }
}

internal class FactoryBean<T>(
    private val share: ShareScope,
    private val clazz: Class<T>,
    private val function: (LookupContext?) -> T
) : Bean<T> {

    fun getValue(context: LookupContext): T {
        val owner = (context as LifecycleLookup).owner

        if (share == ShareScope.Singleton) {
            error("Please use SingletonBean to registry")
        }

        val useDefaultFactory = (share == ShareScope.None)
                || (owner !is ViewModelStoreOwner)
                || (share == ShareScope.Fragment && owner is Activity)

        if (useDefaultFactory) {
            return function(context)
        }

        var shareOwner = owner

        if (share == ShareScope.Activity && owner is Fragment) {
            shareOwner = owner.requireActivity()
        }

        val viewModel = (shareOwner as ViewModelStoreOwner).viewModelStore
            .getOrPut("android:support:di:share") {
                ShareDIInstanceViewModel()
            }
        return viewModel.getOrPut(clazz) { function(context) }
    }

    override fun getValue(): T {
        return function(null)
    }

    private class ShareDIInstanceViewModel : ViewModel() {
        private var mCache = hashMapOf<Class<*>, Any>()

        @Suppress("unchecked_cast")
        fun <T> getOrPut(key: Class<*>, def: () -> T): T {
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
    }
}

internal class ScopeBean<T>(private val function: () -> T) : Bean<T> {
    private var mValue: T? = null

    fun dispose() {
        mValue = null
    }

    override fun getValue(): T {
        if (mValue == null) mValue = function()
        return mValue!!
    }
}

internal class ApplicationBean(private val application: Application) : Bean<Application> {
    override fun getValue(): Application {
        return application
    }
}