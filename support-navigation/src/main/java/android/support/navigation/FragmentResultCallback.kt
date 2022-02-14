package android.support.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment

interface FragmentResultCallback {
    fun onFragmentResult(result: Bundle)
}

fun Fragment.notifyResultIfNeeded(result: Bundle?) {
    if (this is FragmentResultCallback && result != null) {
        lifecycle.addObserver(OnFragmentReadyToNotifyListener {
            onFragmentResult(result)
        })
    }
}