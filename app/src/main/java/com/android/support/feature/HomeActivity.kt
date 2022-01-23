package com.android.support.feature

import android.os.Bundle
import android.support.core.PairLookup
import android.support.core.pairLookupOf
import android.support.core.view.viewBinding
import android.support.navigation.NavOptions
import android.support.navigation.Navigator
import android.support.navigation.findNavigator
import androidx.fragment.app.Fragment
import com.android.support.R
import com.android.support.app.AppActivity
import com.android.support.databinding.ActivityHomeBinding
import com.android.support.feature.help.HelpFragment
import com.android.support.feature.home.HomeFragment
import com.android.support.feature.setting.SettingFragment
import com.android.support.widget.TopBarAdapter
import com.android.support.widget.TopBarAdapterImpl
import com.android.support.widget.TopBarOwner
import kotlin.reflect.KClass

class HomeActivity : AppActivity(R.layout.activity_home), TopBarOwner {
    private val binding by viewBinding(ActivityHomeBinding::bind)
    override lateinit var topBar: TopBarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topBar = TopBarAdapterImpl(this, binding.topBar)
        val navigator = findNavigator()

        @Suppress("unchecked_cast")
        val route = pairLookupOf(
            R.id.mnHome to HomeFragment::class as KClass<Fragment>,
            R.id.mnHelp to HelpFragment::class,
            R.id.mnSetting to SettingFragment::class
        )

        navigator.addDestinationChangeListener {
            binding.navigationBar.selectedItemId = route.requireKeyOf(it)
        }

        binding.navigationBar.setOnItemSelectedListener {
            navigator.navigateTo(it.itemId, route)
        }
        if (savedInstanceState == null) navigator.navigateTo(R.id.mnHome, route)
    }

    private fun Navigator.navigateTo(
        id: Int,
        route: PairLookup<Int, KClass<out Fragment>>
    ): Boolean {
        val des = route.requireValueOf(id)
        val shouldNavigate = lastDestination?.kClass != des
        if (shouldNavigate) navigate(
            des, navOptions = NavOptions(
                popupTo = HomeFragment::class,
                reuseInstance = true,
                inclusive = true
            )
        )
        return shouldNavigate
    }

    override fun onBackPressed() {
        if (!findNavigator().navigateUp()) super.onBackPressed()
    }
}