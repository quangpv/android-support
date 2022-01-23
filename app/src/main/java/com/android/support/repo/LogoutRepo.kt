package com.android.support.repo

import android.support.di.Inject
import android.support.di.ShareScope
import com.android.support.datasource.UserLocalSource

@Inject(ShareScope.Fragment)
class LogoutRepo(private val userLocalSource: UserLocalSource) {
    operator fun invoke() {
        userLocalSource.clearToken()
    }
}