package android.support.applifecycle

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class ApplicationLifecycleOwner : LifecycleOwner {

    private val mRegistry = LifecycleRegistry(this)

    private fun attach(application: Application) {
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        application.registerActivityLifecycleCallbacks(object : ApplicationLifecycleObserver() {
            override fun onCreate() {
                mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }

            override fun onStart() {
                mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            override fun onStop() {
                mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }

            override fun onDestroy() {
                mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
        })
    }

    companion object {
        private val instance = ApplicationLifecycleOwner()

        fun get(): ApplicationLifecycleOwner {
            return instance
        }

        internal fun init(context: Context) {
            instance.attach(context.applicationContext as Application)
        }
    }

    override fun getLifecycle(): Lifecycle {
        return mRegistry
    }
}