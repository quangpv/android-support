package android.support.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface ArgumentChangeable {
    fun onNewArguments(arguments: Bundle)
}

internal fun Fragment.notifyArgumentChangeIfNeeded(args: Bundle?) {
    arguments = args
    args ?: return
    if (this !is ArgumentChangeable) return
    if (isAdded) {
        onNewArguments(args)
        return
    }
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            lifecycle.removeObserver(this)
            onNewArguments(args)
        }
    })
}