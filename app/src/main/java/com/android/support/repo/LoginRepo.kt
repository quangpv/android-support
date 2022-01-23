package com.android.support.repo

import android.support.di.InjectBy
import android.support.di.Injectable
import android.support.di.ShareScope
import com.android.support.datasource.AuthenticateApi
import com.android.support.datasource.UserLocalSource
import com.android.support.helper.Validator
import com.android.support.model.LoginForm

@InjectBy(LoginRepoV1::class, ShareScope.Activity)
interface LoginRepo {
    suspend operator fun invoke(form: LoginForm)
}

class LoginRepoV1(
    private val userLocalSource: UserLocalSource,
    private val authenticateApi: AuthenticateApi,
    private val validator: Validator
) : LoginRepo, Injectable {
    override suspend operator fun invoke(form: LoginForm) {
        with(validator) {
            checkEmail(form.email)
            checkPassword(form.password)
        }

        val token = authenticateApi.login(form.email, form.password).await()
        userLocalSource.saveToken(token)
    }
}
