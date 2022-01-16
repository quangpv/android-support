package android.support.navigation

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

fun interface OnDestinationChangeListener {
    fun onDestinationChanged(destination: KClass<out Fragment>)
}