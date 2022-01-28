package com.android.support.functional

import com.android.support.widget.ErrorDialogOwner

interface NotSupportable : ErrorDialogOwner {
    fun notSupport() {
        errorDialog.show("Not support")
    }
}