package com.android.support.feature.setting

import android.os.Bundle
import android.support.core.livedata.SingleLiveEvent
import android.support.core.livedata.call
import android.support.core.route.close
import android.support.core.view.viewBinding
import android.support.viewmodel.launch
import android.support.viewmodel.viewModel
import android.view.View
import androidx.lifecycle.ViewModel
import com.android.support.R
import com.android.support.app.AppFragment
import com.android.support.databinding.FragmentSettingBinding
import com.android.support.navigation.Router
import com.android.support.navigation.Routing
import com.android.support.repo.LogoutRepo
import com.android.support.widget.TextCenterTopBarState
import com.android.support.widget.TopBarOwner

class SettingFragment : AppFragment(R.layout.fragment_setting), TopBarOwner {
    private val viewModel by viewModel<SettingViewModel>()
    private val binding by viewBinding(FragmentSettingBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogout.setOnClickListener { viewModel.logout() }
        viewModel.logoutSuccess.bind {
            Router.open(self, Routing.Login)
            close()
        }
        topBar.setState(TextCenterTopBarState(R.string.title_setting))
    }

    class SettingViewModel(private val logoutRepo: LogoutRepo) : ViewModel() {
        val logoutSuccess = SingleLiveEvent<Any>()

        fun logout() = launch {
            logoutRepo()
            logoutSuccess.call()
        }
    }
}
