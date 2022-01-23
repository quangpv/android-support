package com.android.support.datasource

import android.content.Context
import android.support.di.Inject
import android.support.di.ShareScope
import android.support.persistent.cache.Caching
import android.support.persistent.cache.GsonParser

@Inject(ShareScope.Singleton)
class AppCache(
    private val context: Context,
) : Caching(context, GsonParser()) {

    var email: String by string("email", "")
    var password: String by string("password", "")
    var token: String by string("token", "")
}