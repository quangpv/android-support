package android.support.navigation

import android.support.navigation.v2.FragmentNavigator
import androidx.fragment.app.FragmentManager

interface FragmentNavigatorFactory {
    fun create(manager: FragmentManager, containerId: Int): Navigator
}

class FragmentNavigatorFactoryV2 : FragmentNavigatorFactory {
    override fun create(manager: FragmentManager, containerId: Int): Navigator {
        return FragmentNavigator(manager, containerId)
    }
}