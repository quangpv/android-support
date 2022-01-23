package com.android.support.helper

import android.support.di.InjectBy
import android.support.di.Injectable
import android.support.di.ShareScope
import com.android.support.R
import com.android.support.exception.viewError

@InjectBy(ValidatorImpl::class, ShareScope.Singleton)
interface Validator {

    fun checkEmail(email: String)

    fun checkPassword(password: String)
}

class ValidatorImpl : Validator, Injectable {
    override fun checkEmail(email: String) {
        if (email.isBlank()) viewError(R.id.edtUserName, "Email should not be blank")
    }

    override fun checkPassword(password: String) {
        if (password.isBlank()) viewError(R.id.edtPassword, "Password should not be blank")
    }
}