package com.android.support

import android.os.Bundle
import android.support.core.livedata.SingleLiveEvent
import android.support.core.livedata.call
import android.support.core.route.close
import android.support.core.route.open
import android.support.viewmodel.launch
import android.support.viewmodel.viewModel
import androidx.lifecycle.ViewModel
import com.android.support.app.AppActivity
import com.android.support.feature.HomeActivity
import com.android.support.navigation.Router
import com.android.support.navigation.Routing
import com.android.support.repo.auth.CheckLoginRepo

class MainActivity : AppActivity(0) {
    private val viewModel by viewModel<VM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.login.bind {
            Router.open(self, Routing.Login)
            close()
        }

        viewModel.main.bind {
            open<HomeActivity>().close()
        }
    }

    class VM(private val isLoggedInRepo: CheckLoginRepo) : ViewModel() {

        val main = SingleLiveEvent<Any>()
        val login = SingleLiveEvent<Any>()

        init {
            launch {
                if (isLoggedInRepo()) main.call()
                else login.call()
            }
        }
    }
}