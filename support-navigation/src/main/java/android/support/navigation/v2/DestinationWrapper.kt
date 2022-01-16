package android.support.navigation.v2

import android.support.navigation.Destination
import androidx.fragment.app.Fragment

class DestinationWrapper(
    val destination: Destination,
    val fragment: Fragment,
    val isUpdateCurrent: Boolean = false
)