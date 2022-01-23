package android.support.navigation.v2

import android.os.Bundle
import android.support.navigation.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlin.reflect.KClass

class FragmentNavigator(
    fragmentManager: FragmentManager,
    container: Int
) : Navigator(fragmentManager, container) {

    private val mStack = DestinationStack()

    override val lastDestination: Destination? get() = mStack.last

    init {

        mStack.onPopBackStackListener = {
            if (!it.keepInstance) tagManager.remove(it.kClass)
        }
    }

    override fun onSaveInstance(state: Bundle) {
        super.onSaveInstance(state)
        mStack.onSaveInstance(state)
    }

    override fun onRestoreInstance(saved: Bundle) {
        super.onRestoreInstance(saved)
        mStack.onRestoreInstance(saved)
    }

    override fun navigate(kClass: KClass<out Fragment>, args: Bundle?, navOptions: NavOptions?) {
        transaction {

            val wrapper = lookupDestination(kClass, navOptions)
            val fragment = wrapper.fragment
            val destination = wrapper.destination

            fragment.notifyArgumentChangeIfNeeded(args)
            if (wrapper.isUpdateCurrent) return@transaction

            val lastVisible = mStack.last
            setNavigateAnim(destination, lastVisible, mStack.isEmpty)

            executePopFragment(fragment, lastVisible?.requireFragment, doPopBackStack(navOptions))

            if (fragment.isDetached) show(fragment)
            else add(container, fragment, destination.tag)

            mStack.push(destination)
            notifyDestinationChange(kClass)
        }
    }

    private fun SupportFragmentTransaction.executePopFragment(
        enter: Fragment,
        exit: Fragment?,
        removes: List<DestinationWrapper>
    ) {
        var exitInRemoves = false

        removes.forEach {
            if (it.fragment != enter) remove(it.fragment)
            if (it.fragment == exit) exitInRemoves = true
        }

        if (!exitInRemoves && exit != null) hide(exit)
    }

    private fun lookupDestination(
        kClass: KClass<out Fragment>,
        navOptions: NavOptions?,
    ): DestinationWrapper {
        val lastDes = mStack.last
        val isSingleTask = navOptions?.singleTask == true
        val isReuseInstance = navOptions?.reuseInstance == true

        if (kClass == lastDes?.kClass) {
            if (isSingleTask || (isReuseInstance && lastDes.keepInstance))
                return DestinationWrapper(lastDes, lastDes.requireFragment, true)
        }
        if (isSingleTask) {
            val singleTaskDes = mStack.find(kClass)
            if (singleTaskDes != null) {
                return DestinationWrapper(singleTaskDes, singleTaskDes.requireFragment)
            }
        }

        return if (isReuseInstance) createKeepInstanceDestination(kClass, navOptions!!)
        else createDestination(kClass, navOptions)
    }

    private fun createDestination(
        kClass: KClass<out Fragment>,
        navOptions: NavOptions?
    ): DestinationWrapper {
        val reuseFragment: Fragment? =
            if (navOptions?.singleTask == true) mStack.find(kClass)?.fragment else null
        val tagId = tagManager.getTagId(reuseFragment)
        val des = Destination(kClass, tagId, navOptions)
        tagManager.save(des.tag, kClass)
        return DestinationWrapper(des, reuseFragment ?: des.createFragment())
    }

    private fun createKeepInstanceDestination(
        kClass: KClass<out Fragment>,
        navOptions: NavOptions
    ): DestinationWrapper {
        val tagId = tagManager.getTagId(kClass)
        val des = Destination(kClass, tagId, navOptions)
        tagManager.save(des.tag, kClass)
        return DestinationWrapper(des, des.fragment ?: des.createFragment())
    }

    private fun doPopBackStack(
        navOptions: NavOptions?
    ): List<DestinationWrapper> {
        navOptions?.popupTo ?: return emptyList()
        return doPopBackStack(navOptions.popupTo, navOptions.inclusive)
    }

    private fun doPopBackStack(
        popupTo: KClass<out Fragment>,
        inclusive: Boolean
    ): List<DestinationWrapper> {
        val removes = arrayListOf<DestinationWrapper>()

        mStack.popBack(object : DestinationStack.OnPopListener {
            override fun shouldNext(des: Destination): Boolean {
                return des.kClass != popupTo
            }

            override fun shouldPop(des: Destination): Boolean {
                if (des.kClass != popupTo) return true
                if (inclusive) return true
                return false
            }

            override fun onPop(des: Destination) {
                if (des.keepInstance) return
                removes.add(DestinationWrapper(des, des.requireFragment))
            }
        })

        return removes.filter {
            !mStack.hasTagId(it.destination.tagId)
        }
    }

    override fun popBackStack(popupTo: KClass<out Fragment>, inclusive: Boolean): Boolean {
        if (mStack.size <= 1) return false
        val target = mStack.find(popupTo) ?: return false

        val exitDes = mStack.last!!

        if (target == exitDes) {
            if (!inclusive) return false
            return navigateUp()
        }

        val exitFragment = exitDes.requireFragment
        val removes = doPopBackStack(popupTo, inclusive)
        transaction {
            val enterDes = if (mStack.isEmpty) target else mStack.last!!
            val enterFragment = enterDes.requireFragment
            executePopFragment(enterFragment, exitFragment, removes)
            show(enterFragment)
            if (mStack.isEmpty) mStack.push(enterDes)
            notifyDestinationChange(enterDes.kClass)
        }
        return true
    }

    override fun navigateUp(result: Bundle?): Boolean {
        if (mStack.isEmpty) return false
        val currentFragment = mStack.last!!.requireFragment
        if (currentFragment is Backable && currentFragment.onInterceptBackPress()) return true

        if (mStack.size == 1) return false
        val current = mStack.pop() ?: return false
        val previous = mStack.last
        transaction {
            setPopupAnim(current, previous)
            val shouldHide = current.keepInstance || mStack.hasTagId(current.tagId)

            if (shouldHide) hide(currentFragment)
            else remove(currentFragment)

            if (previous != null) {
                val fragment = previous.requireFragment
                show(fragment)
                fragment.notifyResultIfNeeded(result)
                notifyDestinationChange(fragment.javaClass.kotlin)
            }
        }
        return previous != null
    }
}
