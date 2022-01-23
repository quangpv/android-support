package com.android.support

import android.app.Application
import android.support.core.savedstate.SavedStateHandlerFactory
import android.support.di.LifecycleLookup
import android.support.di.ShareScope
import android.support.di.dependencies
import com.android.support.app.appModule

@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        dependencies {
            factory(shareIn = ShareScope.FragmentOrActivity) {
                if (this !is LifecycleLookup) error("Saved state handler just support for LifecycleLookup")
                SavedStateHandlerFactory(this.owner).create()
            }
            modules(appModule)
        }
    }
}
