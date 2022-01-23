package com.android.support.app

import android.support.core.event.WindowStatusOwner
import android.support.core.extensions.LifecycleSubscriberExt
import android.support.core.route.RouteDispatcher
import android.support.viewmodel.ViewModelRegistrable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.android.support.exception.ErrorHandler
import com.android.support.exception.ErrorHandlerImpl
import com.android.support.functional.NotSupportable

abstract class AppFragment(contentLayoutId: Int) : Fragment(contentLayoutId),
    RouteDispatcher,
    LifecycleSubscriberExt,
    ViewModelRegistrable,
    NotSupportable,
    AppPermissionOwner,
    ErrorHandler by ErrorHandlerImpl() {

    val self get() = this
    override fun onRegistryViewModel(viewModel: ViewModel) {

    }

    override fun onReRegistryViewModel(viewModel: ViewModel) {
        if (viewModel is WindowStatusOwner) {
            viewModel.loading.bind {

            }
            viewModel.error.bind {
                handle(this, it)
            }
        }
    }
}