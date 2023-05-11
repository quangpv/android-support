package com.android.support.helper

import android.content.Context
import android.support.core.lifecycle.LifecycleOwnerDelegate
import android.support.di.ScopeLookup
import android.support.di.ShareScope
import android.support.di.module
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

interface ResourceResolver {
    fun getString(res: Int): String

    companion object {
        fun module(useMultiLanguage: Boolean = false) = module {
            if (useMultiLanguage) factory(shareIn = ShareScope.FragmentOrActivity) {
                LifecycleOwnerDelegate.of((this as ScopeLookup).owner as LifecycleOwner)
            }
            factory(shareIn = ShareScope.Activity) {
                if (useMultiLanguage) MultiLanguageResourceResolver(get())
                else ApplicationResourceResolver(get())
            }
        }
    }
}

class MultiLanguageResourceResolver(private val ownerRef: LifecycleOwnerDelegate) :
    ResourceResolver {
    private val context: Context
        get() = ownerRef.get().let {
            if (it is Fragment) it.requireContext()
            else it as Context
        }

    override fun getString(res: Int): String {
        return context.getString(res)
    }
}

class ApplicationResourceResolver(private val context: Context) : ResourceResolver {
    override fun getString(res: Int): String {
        return context.getString(res)
    }
}