package android.support.di

import androidx.lifecycle.LifecycleOwner

interface LifecycleLookup {
    val owner: LifecycleOwner
}