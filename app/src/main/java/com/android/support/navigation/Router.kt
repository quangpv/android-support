package com.android.support.navigation

import android.support.core.route.BundleArgument
import android.support.core.route.RouteDispatcher
import android.support.core.route.open
import android.support.navigation.NavOptions
import android.support.navigation.findNavigator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.support.app.AppConfig
import com.android.support.feature.MainNavigationActivity
import com.android.support.feature.auth.LoginFragment
import com.android.support.feature.random.RandomDetailFragment
import com.android.support.functional.NotSupportable
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass

interface Routing : BundleArgument {
    val fragmentClass: KClass<out Fragment>
    val options: NavOptions? get() = null

    @Parcelize
    object Login : Routing {
        override val fragmentClass: KClass<out Fragment>
            get() = LoginFragment::class
    }

    @Parcelize
    class RandomDetail(val id: String, val folderId: Int) : Routing {
        override val fragmentClass: KClass<out Fragment>
            get() = RandomDetailFragment::class
    }

}

interface Router {
    fun RouteDispatcher.notSupport() {
        return (this as NotSupportable).notSupport()
    }

    fun open(dispatcher: RouteDispatcher, route: Routing)
    fun navigate(dispatcher: RouteDispatcher, route: Routing)

    companion object : Router by AppConfig.createRoute()
}

open class DevRouter : Router {
    override fun open(dispatcher: RouteDispatcher, route: Routing) {
        dispatcher.open<MainNavigationActivity>(route)
    }

    override fun navigate(dispatcher: RouteDispatcher, route: Routing) {
        val navigator = when (dispatcher) {
            is FragmentActivity -> dispatcher.findNavigator()
            is Fragment -> dispatcher.findNavigator()
            else -> error("Not found navigator")
        }
        navigator.navigate(route.fragmentClass, route.toBundle(), route.options)
    }
}

class ProRouter : DevRouter() {
    override fun open(dispatcher: RouteDispatcher, route: Routing) {
        when (route) {
            is Routing.Login -> dispatcher.notSupport()
            else -> super.open(dispatcher, route)
        }
    }

    override fun navigate(dispatcher: RouteDispatcher, route: Routing) {
        when (route) {
            is Routing.Login -> dispatcher.notSupport()
            else -> super.navigate(dispatcher, route)
        }
    }
}