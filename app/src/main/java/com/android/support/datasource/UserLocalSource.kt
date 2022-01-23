package com.android.support.datasource

import android.support.di.Inject
import android.support.di.ShareScope
import com.sample.app.model.entity.AccountEntity

@Inject(ShareScope.Singleton)
class UserLocalSource(private val appCache: AppCache) {
    fun getToken(): String {
        return appCache.token
    }

    fun saveToken(token: String) {
        appCache.token = token
    }

    fun clearToken() {
        appCache.token = ""
    }

    fun saveAccount(email: AccountEntity) {
        appCache.email = email.email
        appCache.password = email.password
    }

    fun clearAccount() {
        appCache.email = ""
        appCache.password = ""
    }

    fun getAccount(): AccountEntity? {
        if (appCache.email.isBlank()) return null
        return AccountEntity(appCache.email, appCache.password)
    }
}