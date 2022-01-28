package com.android.support.feature

import android.os.Bundle
import android.support.core.route.argument
import android.support.navigation.findNavigator
import com.android.support.R
import com.android.support.app.AppActivity
import com.android.support.navigation.Router

class MainNavigationActivity : AppActivity(R.layout.activity_main_navigation) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            Router.navigate(self, argument())
        }
    }

    override fun onBackPressed() {
        if (!findNavigator().navigateUp()) {
            super.onBackPressed()
        }
    }
}