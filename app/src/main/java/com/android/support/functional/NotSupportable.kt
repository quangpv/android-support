package com.android.support.functional

interface NotSupportable : ErrorDialogOwner {
    fun notSupport() {
        errorDialog.show("Not support")
    }
}