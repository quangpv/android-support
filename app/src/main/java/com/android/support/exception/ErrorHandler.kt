package com.android.support.exception

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.support.core.route.RouteDispatcher
import android.support.viewmodel.launch
import android.support.viewmodel.viewModel
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import com.android.support.app.AppActivity
import com.android.support.app.AppFragment
import com.android.support.app.Cleaner
import com.android.support.app.TokenExpiredException
import com.android.support.navigation.Router
import com.android.support.navigation.Routing

interface ErrorHandler {
    fun handle(activity: AppActivity, error: Throwable)
    fun handle(activity: AppFragment, error: Throwable)
}

class ErrorHandlerImpl : ErrorHandler {

    override fun handle(activity: AppActivity, error: Throwable) {
        handle(activity as RouteDispatcher, error)
    }

    private fun handle(dispatcher: RouteDispatcher, error: Throwable) {
        if (error is ViewError) {
            val view = if (dispatcher is AppFragment) {
                dispatcher.requireView().findViewById<EditText>(error.id)
            } else {
                (dispatcher as Activity).findViewById<EditText>(error.id)
            }
            view.error = error.message
            return
        }
        if (error is TokenExpiredException) {
            val viewModel by viewModel<ErrorHandlerViewModel> { dispatcher as ViewModelStoreOwner }
            viewModel.cleanup {
                Router.open(dispatcher, Routing.Login)
            }
            return
        }
        AlertDialog.Builder(
            if (dispatcher is Fragment) dispatcher.requireContext()
            else dispatcher as Context
        ).setMessage(error.message)
            .create()
            .show()
    }

    override fun handle(activity: AppFragment, error: Throwable) {
        handle(activity as RouteDispatcher, error)
    }

    class ErrorHandlerViewModel(private val cleaner: Cleaner) : ViewModel() {
        fun cleanup(function: () -> Unit) {
            launch {
                cleaner.cleanup()
                function()
            }
        }
    }
}

