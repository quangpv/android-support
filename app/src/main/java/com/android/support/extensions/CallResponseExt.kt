package com.android.support.extensions

class ApiResponse<T>(
    val data: T,
    val message: String? = null,
    val statusCode: Int? = null
)