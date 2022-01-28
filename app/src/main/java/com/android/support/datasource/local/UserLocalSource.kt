package com.android.support.datasource.local

import android.content.Context
import android.support.di.Inject
import android.support.di.ShareScope
import android.support.persistent.cache.GsonCaching
import com.sample.app.model.entity.AccountEntity

@Inject(ShareScope.Singleton)
class UserLocalSource(private val context: Context) {
    private val caching = GsonCaching(context)

    private var mToken: String by caching.string("token", "")
    private var email: String by caching.string("email", "")
    private var password: String by caching.string("password", "")

    fun getToken(): String {
        return mToken
    }

    fun saveToken(token: String) {
        mToken = token
    }

    fun clearToken() {
        mToken = ""
    }

    fun saveAccount(account: AccountEntity) {
        this.email = account.email
        this.password = account.password
    }

    fun clearAccount() {
        email = ""
        password = ""
    }

    fun getAccount(): AccountEntity? {
        if (email.isBlank()) return null
        return AccountEntity(email, password)
    }
}