package android.support.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment

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
    lifecycle.addObserver(OnFragmentReadyToNotifyListener {
        onNewArguments(args)
    })
}