package com.android.support.repo.auth

import android.support.di.Inject
import android.support.di.ShareScope
import com.android.support.datasource.local.UserLocalSource

@Inject(ShareScope.Fragment)
class LogoutRepo(private val userLocalSource: UserLocalSource) {
    operator fun invoke() {
        userLocalSource.clearToken()
    }
}