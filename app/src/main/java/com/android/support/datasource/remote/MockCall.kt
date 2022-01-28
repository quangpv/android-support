package com.android.support.datasource.remote

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MockCall<T>(private val mockData: T) : Call<T> {
    override fun isExecuted(): Boolean {
        return true
    }

    override fun cancel() {
    }

    override fun isCanceled(): Boolean {
        return false
    }

    override fun request(): Request {
        return Request.Builder().build()
    }

    override fun timeout(): Timeout {
        return Timeout()
    }

    override fun clone(): Call<T> {
        return MockCall(mockData)
    }

    override fun execute(): Response<T> {
        return Response.success(mockData)
    }

    override fun enqueue(callback: Callback<T>) {
        callback.onResponse(this, Response.success(mockData))
    }
}

