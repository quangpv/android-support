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
import com.android.support.helper.LanguageUtils
import com.android.support.helper.ResourceResolver
import com.android.support.model.ui.LoginForm
import com.android.support.repo.account.GetSavedAccountRepo
import com.android.support.repo.auth.LoginRepo
import com.android.support.repo.account.SaveAccountRepo
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
        binding.btnChangeLanguage.setOnClickListener {
            viewModel.fetchChangeLanguageTitle()
        }

        with(viewModel) {
            viewLoading.bind(binding.btnLogin::setEnabled) { !this }
            account.bind(binding.edtPassword::setText) { password }
            account.bind(binding.edtUserName::setText) { email }
            account.bind(binding.cbSaveAccount::setChecked) { save }
            loginSuccess.bind {
                open<HomeActivity>()
            }
            changeLanguage.bind { title ->
                ChangeLanguageDialog(requireContext()).show(title) {
                    LanguageUtils.setLocale(requireActivity(), it.code)
                    requireActivity().recreate()
                }
            }
        }
    }

}

class LoginViewModel(
    private val getSavedAccountRepo: GetSavedAccountRepo,
    private val saveAccountRepo: SaveAccountRepo,
    private val loginRepo: LoginRepo,
    private val applicationScope: ApplicationScope,
    private val resourceResolver: ResourceResolver,
) : ViewModel(),
    WindowStatusOwner by LiveDataStatusOwner() {
    val account = getSavedAccountRepo.result
    val form = account.map { it as LoginForm }
    val viewLoading: LoadingEvent = LoadingLiveData()

    val loginSuccess = SingleLiveEvent<Int>()
    val changeLanguage = SingleLiveEvent<String>()

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

    fun fetchChangeLanguageTitle() {
        changeLanguage.post(resourceResolver.getString(R.string.title_change_language))
    }
}