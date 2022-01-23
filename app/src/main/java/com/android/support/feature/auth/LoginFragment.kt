package com.android.support.feature.auth

import android.os.Bundle
import android.support.applifecycle.ApplicationScope
import android.support.core.event.LiveDataStatusOwner
import android.support.core.event.LoadingEvent
import android.support.core.event.WindowStatusOwner
import android.support.core.livedata.LoadingLiveData
import android.support.core.livedata.SingleLiveEvent
import android.support.core.livedata.map
import android.support.core.livedata.post
import android.support.core.route.open
import android.support.core.view.viewBinding
import android.support.viewmodel.launch
import android.support.viewmodel.viewModel
import android.view.View
import androidx.lifecycle.ViewModel
import com.android.support.R
import com.android.support.app.AppFragment
import com.android.support.databinding.FragmentLoginBinding
import com.android.support.extensions.bind
import com.android.support.feature.HomeActivity
import com.android.support.model.LoginForm
import com.android.support.repo.GetSavedAccountRepo
import com.android.support.repo.LoginRepo
import com.android.support.repo.SaveAccountRepo
import kotlinx.coroutines.launch

class LoginFragment : AppFragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)
    private val viewModel by viewModel<LoginViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            viewModel.form.bind {
                edtPassword.bind(it::password::set)
                edtUserName.bind(it::email::set)
                cbSaveAccount.bind(it::save::set)
            }
        }

        binding.btnLogin.setOnClickListener {
            viewModel.login()
        }

        with(viewModel) {
            viewLoading.bind(binding.btnLogin::setEnabled) { !this }
            account.bind(binding.edtPassword::setText) { password }
            account.bind(binding.edtUserName::setText) { email }
            account.bind(binding.cbSaveAccount::setChecked) { save }
            loginSuccess.bind { open<HomeActivity>() }
        }
    }

}

class LoginViewModel(
    private val getSavedAccountRepo: GetSavedAccountRepo,
    private val saveAccountRepo: SaveAccountRepo,
    private val loginRepo: LoginRepo,
    private val applicationScope: ApplicationScope
) : ViewModel(),
    WindowStatusOwner by LiveDataStatusOwner() {
    val account = getSavedAccountRepo.result
    val form = account.map { it as LoginForm }
    val viewLoading: LoadingEvent = LoadingLiveData()

    val loginSuccess = SingleLiveEvent<Int>()

    fun login() = launch(viewLoading, error) {
        loginRepo(form.value ?: return@launch)
        loginSuccess.post(R.string.login_success)
    }

    override fun onCleared() {
        super.onCleared()
        applicationScope.launch {
            saveAccountRepo()
        }
    }
}