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
    fun checkStatus(status: String)
    fun checkName(name: String)
    fun checkFolderId(folderId: String)
}

class ValidatorImpl : Validator, Injectable {
    override fun checkEmail(email: String) {
        if (email.isBlank()) viewError(R.id.edtUserName, "Email should not be blank")
    }

    override fun checkPassword(password: String) {
        if (password.isBlank()) viewError(R.id.edtPassword, "Password should not be blank")
    }

    override fun checkStatus(status: String) {
        if (status.isBlank()) viewError(R.id.edtStatus, "Status should not be blank")
    }

    override fun checkName(name: String) {
        if (name.isBlank()) viewError(R.id.edtName, "Name should not be blank")
    }

    override fun checkFolderId(folderId: String) {
        if (folderId.toIntOrNull() == null) viewError(R.id.edtFolderId, "Folder id invalid")
    }
}