package android.support.di

import androidx.lifecycle.LifecycleOwner

internal interface LifecycleLookup {
    val owner: LifecycleOwner
}