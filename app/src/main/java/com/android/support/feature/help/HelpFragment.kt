package com.android.support.feature.help

import android.os.Bundle
import android.support.core.livedata.post
import android.support.core.view.viewBinding
import android.support.di.InjectScope
import android.support.di.NamedScope
import android.support.viewmodel.launch
import android.support.viewmodel.viewModel
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.support.R
import com.android.support.app.AppFragment
import com.android.support.app.DIScope
import com.android.support.databinding.FragmentHelpBinding
import com.android.support.widget.TextCenterTopBarState
import com.android.support.widget.TopBarOwner
import com.google.android.material.tabs.TabLayout
import kotlin.random.Random

@NamedScope(DIScope.HELP)
class HelpFragment : AppFragment(R.layout.fragment_help), TopBarOwner {
    private val binding by viewBinding(FragmentHelpBinding::bind)
    private val viewModel by viewModel<VM>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) viewModel.load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pageAdapter = PagerAdapter(binding.viewPage, this)
        pageAdapter.setupWith(binding.tabHelp)
        binding.viewRefresh.setOnRefreshListener {
            binding.viewRefresh.isRefreshing = false
            viewModel.refresh()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        topBar.setState(TextCenterTopBarState(R.string.title_help))
    }

    class PagerAdapter(private val viewPager: ViewPager2, fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        init {
            viewPager.adapter = this
        }

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ContactHelpFragment()
                else -> SupportHelpFragment()
            }
        }

        fun setupWith(tabHelp: TabLayout) {
            tabHelp.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    if (tab.position == viewPager.currentItem) return
                    viewPager.currentItem = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (tabHelp.selectedTabPosition == position) return
                    tabHelp.getTabAt(position)?.select()
                }
            })
        }
    }

    class VM(private val fetchUserRepo: FetchUserRepo) : ViewModel() {

        fun load() = launch {
            fetchUserRepo()
        }

        fun refresh() = launch {
            fetchUserRepo()
        }

    }
}

@InjectScope(DIScope.HELP)
class FetchUserRepo {
    val result = MutableLiveData<IUser>()

    operator fun invoke() {
        result.post(object : IUser {
            override val name: String = "My ${Random.nextInt()}"
        })
    }
}

interface IUser {
    val name: String
}