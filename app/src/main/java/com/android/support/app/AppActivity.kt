package com.android.support.app

import android.support.core.event.WindowStatusOwner
import android.support.core.extensions.LifecycleSubscriberExt
import android.support.core.route.RouteDispatcher
import android.support.di.ScopeOwner
import android.support.viewmodel.ViewModelRegistrable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.android.support.exception.ErrorHandler
import com.android.support.exception.ErrorHandlerImpl

abstract class AppActivity(contentLayoutId: Int = 0) : AppCompatActivity(contentLayoutId),
    LifecycleSubscriberExt,
    ViewModelRegistrable,
    RouteDispatcher,
    AppPermissionOwner, ScopeOwner,
    ErrorHandler by ErrorHandlerImpl() {

    val self get() = this

    override fun onRegistryViewModel(viewModel: ViewModel) {
        if (viewModel is WindowStatusOwner) {
            viewModel.error.bind { handle(this, it) }
            viewModel.loading.bind {
                // show-loading
            }
        }
    }
}
