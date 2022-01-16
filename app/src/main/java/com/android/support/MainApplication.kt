package com.android.support

import android.support.di.dependencies
import androidx.multidex.MultiDexApplication

class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        dependencies { }
    }
}