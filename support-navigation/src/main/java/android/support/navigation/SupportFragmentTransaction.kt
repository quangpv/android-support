package android.support.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

interface SupportFragmentTransaction {

    fun setNavigateAnim(
        enter: Destination,
        exit: Destination?,
        isStart: Boolean
    )

    fun setPopupAnim(
        exit: Destination,
        enter: Destination?
    )

    fun add(containerId: Int, fragment: Fragment, tag: String)
    fun remove(fragment: Fragment)
    fun hide(fragment: Fragment)
    fun show(fragment: Fragment)
}

class SupportFragmentTransactionImpl(
    private val transaction: FragmentTransaction,
) : SupportFragmentTransaction {

    override fun setNavigateAnim(
        enter: Destination,
        exit: Destination?,
        isStart: Boolean
    ) {
        transaction.setCustomAnimations(
            if (isStart) 0 else enter.animEnter,
            exit?.animExit ?: 0
        )
    }

    override fun setPopupAnim(
        exit: Destination,
        enter: Destination?
    ) {
        transaction.setCustomAnimations(enter?.animPopEnter ?: 0, exit.animPopExit)
    }

    override fun add(containerId: Int, fragment: Fragment, tag: String) {
        transaction.add(containerId, fragment, tag)
    }

    override fun remove(fragment: Fragment) {
        transaction.remove(fragment)
    }

    override fun hide(fragment: Fragment) {
        transaction.detach(fragment)
    }

    override fun show(fragment: Fragment) {
        transaction.attach(fragment)
    }
}