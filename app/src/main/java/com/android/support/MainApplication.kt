package com.android.support

import android.support.core.savedstate.SavedStateHandlerFactory
import android.support.di.ScopeLookup
import android.support.di.ShareScope
import androidx.lifecycle.LifecycleOwner
import androidx.multidex.MultiDexApplication
import com.afilabs.support.di.ext.dependencies
import com.android.support.app.appModule
import com.android.support.helper.ResourceResolver

@Suppress("unused")
class MainApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        dependencies {
            factory(shareIn = ShareScope.FragmentOrActivity) {
                SavedStateHandlerFactory((this as ScopeLookup).owner as LifecycleOwner).create()
            }
            modules(appModule, ResourceResolver.module(true))
        }
    }
}
