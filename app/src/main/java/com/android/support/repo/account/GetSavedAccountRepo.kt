package com.android.support.repo.account

import android.support.applifecycle.ApplicationScope
import android.support.core.livedata.post
import android.support.core.savedstate.SavedStateHandler
import android.support.di.Inject
import android.support.di.ShareScope
import androidx.lifecycle.MutableLiveData
import com.android.support.datasource.local.UserLocalSource
import com.android.support.factory.AccountFactory
import com.android.support.model.ui.IAccount
import kotlinx.coroutines.launch

@Inject(ShareScope.Fragment)
class GetSavedAccountRepo(
    private val userLocalSource: UserLocalSource,
    private val savedStateHandler: SavedStateHandler,
    private val applicationScope: ApplicationScope,
    private val accountFactory: AccountFactory,
) {
    val result = MutableLiveData<IAccount>()

    init {
        applicationScope.launch { restore() }
    }

    private fun restore() {
        val savedStateKey = "account"
        var account = savedStateHandler.get<IAccount>(savedStateKey)
        if (account == null) {
            val entity = userLocalSource.getAccount()
            account = accountFactory.createForm(entity)
            savedStateHandler.set(savedStateKey, account)
        }
        result.post(account)
    }
}
