package com.android.support.functional

import android.content.Context
import android.support.core.view.ViewScopeOwner
import android.widget.Toast

interface ErrorDialogOwner : ViewScopeOwner {
    val errorDialog: ErrorDialog
        get() = viewScope.getOr("error-dialog") {
            ErrorDialog(viewScope.context)
        }
}

class ErrorDialog(private val context: Context) {
    fun show(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}