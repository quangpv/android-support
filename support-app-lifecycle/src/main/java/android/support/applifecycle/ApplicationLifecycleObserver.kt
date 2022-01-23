package android.support.applifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal abstract class ApplicationLifecycleObserver : Application.ActivityLifecycleCallbacks {

    private var mCreateCounter = 0
    private var mStartCounter = 0
    private var mResumeCounter = 0

    private var isConfigChanging = false
    private var isStartConfigChanging = false

    final override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        mCreateCounter++
    }

    final override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityPostCreated(activity, savedInstanceState)
        if (mCreateCounter == 1 && !isConfigChanging) {
            onCreate()
        }
    }

    final override fun onActivityDestroyed(activity: Activity) {
        isConfigChanging = activity.isChangingConfigurations
        mCreateCounter -= 1

    }

    final override fun onActivityPostDestroyed(activity: Activity) {
        super.onActivityPostDestroyed(activity)
        if (mCreateCounter == 0 && !isConfigChanging) {
            onDestroy()
        }
    }

    final override fun onActivityStarted(activity: Activity) {
        mStartCounter++
    }

    final override fun onActivityPostStarted(activity: Activity) {
        super.onActivityPostStarted(activity)
        if (mStartCounter == 1 && !isStartConfigChanging) {
            onStart()
        }
    }

    final override fun onActivityStopped(activity: Activity) {
        isStartConfigChanging = activity.isChangingConfigurations
        mStartCounter -= 1
    }

    final override fun onActivityPostStopped(activity: Activity) {
        super.onActivityPostStopped(activity)
        if (mStartCounter == 0 && !isStartConfigChanging) {
            onStop()
        }
    }

    final override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    final override fun onActivityResumed(activity: Activity) {
        mResumeCounter++
    }

    final override fun onActivityPostResumed(activity: Activity) {
        super.onActivityPostResumed(activity)
        if (mResumeCounter == 1) {
            onResume()
        }
    }

    final override fun onActivityPaused(activity: Activity) {
        mResumeCounter--
    }

    final override fun onActivityPostPaused(activity: Activity) {
        super.onActivityPostPaused(activity)
        if (mResumeCounter == 0) {
            onPause()
        }
    }

    protected open fun onCreate() {

    }

    protected open fun onDestroy() {

    }

    protected open fun onStart() {

    }

    protected open fun onStop() {

    }

    protected open fun onResume() {

    }

    protected open fun onPause() {

    }

}