// Do not rename this package
// This using for access module member protected
// androidx.fragment.app
package androidx.fragment.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner

private val FragmentManager.fragmentHost: Any
    get() {
        if (this !is FragmentManagerImpl) error("${this.javaClass.name} is not instance of FragmentManagerImpl")
        val parent = this.parent
        if (parent != null) return parent
        val activity = host.activity ?: error("Activity not attached yet!")
        if (activity !is FragmentActivity) error("Activity ${activity.javaClass.simpleName} should be instance of FragmentActivity")
        return activity
    }

internal val FragmentManager.lifecycle: Lifecycle
    get() = (fragmentHost as LifecycleOwner).lifecycle

internal val FragmentManager.savedStateRegistry: SavedStateRegistry
    get() = (fragmentHost as SavedStateRegistryOwner).savedStateRegistry