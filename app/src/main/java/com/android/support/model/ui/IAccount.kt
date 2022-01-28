package com.android.support.model.ui

import android.os.Parcelable
import com.android.support.R
import com.android.support.exception.viewError
import kotlinx.parcelize.Parcelize

interface IAccount {
    val email: String get() = ""
    val password: String get() = ""
    val save: Boolean get() = false
}

@Parcelize
class LoginForm(
    override var email: String = "",
    override var password: String = "",
    override var save: Boolean = false
) : IAccount, Parcelable {

    fun validate() {
        if (email.isBlank()) viewError(R.id.edtUserName, "Email should not be blank")
        if (password.isBlank()) viewError(R.id.edtPassword, "Password should not be blank")
    }

    fun set(account: IAccount) {
        TODO("Not yet implemented")
    }
}