package com.android.support.datasource

import com.android.support.helper.network.Async
import retrofit2.http.POST

interface AuthenticateApi {

    @POST("login")
    fun login(email: String, password: String): Async<String>
}

class MockAuthenticateApi : AuthenticateApi {
    override fun login(email: String, password: String): Async<String> {
        return MockAsync("Token")
    }
}