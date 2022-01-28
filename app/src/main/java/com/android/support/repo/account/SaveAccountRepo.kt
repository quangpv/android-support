package com.android.support.repo.account

import android.support.core.savedstate.SavedStateHandler
import android.support.di.Inject
import com.android.support.datasource.local.UserLocalSource
import com.android.support.model.ui.IAccount
import com.sample.app.model.entity.AccountEntity

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