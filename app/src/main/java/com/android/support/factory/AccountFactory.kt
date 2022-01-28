package com.android.support.factory

import android.support.di.Inject
import android.support.di.ShareScope
import com.android.support.model.ui.LoginForm
import com.sample.app.model.entity.AccountEntity

@Inject(ShareScope.Activity)
class AccountFactory {

    fun createForm(account: AccountEntity?): LoginForm {
        if (account != null)
            return LoginForm(account.email, account.password, true)
        return LoginForm()
    }
}