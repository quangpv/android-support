package com.android.support

import android.support.core.savedstate.SavedStateHandlerFactory
import android.support.di.LifecycleLookup
import android.support.di.ShareScope
import android.support.di.dependencies
import androidx.multidex.MultiDexApplication
import com.android.support.app.appModule
import com.android.support.helper.ResourceResolver

@Suppress("unused")
class MainApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        dependencies {
            factory(shareIn = ShareScope.FragmentOrActivity) {
                SavedStateHandlerFactory((this as LifecycleLookup).owner).create()
            }
            modules(appModule, ResourceResolver.module(true))
        }
    }
}
