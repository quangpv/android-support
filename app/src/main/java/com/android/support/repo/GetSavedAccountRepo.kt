package com.android.support.repo

import android.support.applifecycle.ApplicationScope
import android.support.core.livedata.post
import android.support.core.savedstate.SavedStateHandler
import android.support.di.Inject
import android.support.di.ShareScope
import androidx.lifecycle.MutableLiveData
import com.android.support.datasource.UserLocalSource
import com.android.support.model.IAccount
import com.android.support.model.LoginForm
import com.sample.app.model.entity.AccountEntity
import kotlinx.coroutines.launch

@Inject(ShareScope.Fragment)
class GetSavedAccountRepo(
    private val userLocalSource: UserLocalSource,
    private val savedStateHandler: SavedStateHandler,
    private val applicationScope: ApplicationScope
) {
    val result = MutableLiveData<IAccount>()

    init {
        applicationScope.launch { restore() }
    }

    private fun restore() {
        var account = savedStateHandler.get<IAccount>("account")
        if (account == null) {
            account = userLocalSource.getAccount()?.let {
                LoginForm(it.email, it.password, true)
            }
            savedStateHandler.set("account", account)
        }
        result.post(account ?: LoginForm())
    }
}

@Inject
class SaveAccountRepo(
    private val savedStateHandler: SavedStateHandler,
    private val userLocalSource: UserLocalSource,
) {
    operator fun invoke() {
        val form = savedStateHandler.get<IAccount>("account") ?: error("Account not set yet")
        if (form.save) {
            userLocalSource.saveAccount(AccountEntity(form.email, form.password))
        } else {
            userLocalSource.clearAccount()
        }
    }
}