package com.android.support.datasource.remote

import com.android.support.helper.network.Async

class MockAsync<T>(private val value: T?) : Async<T> {
    override suspend fun awaitNullable(): T? {
        return value
    }

    override suspend fun clone(): Async<T> {
        return this
    }
}