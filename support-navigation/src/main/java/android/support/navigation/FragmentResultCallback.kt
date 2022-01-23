package android.support.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface FragmentResultCallback {
    fun onFragmentResult(result: Bundle)
}

fun Fragment.notifyResultIfNeeded(result: Bundle?) {
    if (this is FragmentResultCallback && result != null) {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                owner.lifecycle.removeObserver(this)
                onFragmentResult(result)
            }
        })
    }
}