package com.android.support.helper.network

import com.android.support.app.ApiRequestException
import com.android.support.extensions.ApiResponse
import retrofit2.Call
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface ApiAsync<T> : Async<T>

class ApiAsyncAdapterFactory(errorHandler: ApiCallErrorHandler) :
    AsyncAdapterFactory(errorHandler) {
    override fun convert(
        call: Call<Any>,
        returnType: Type,
        annotations: Array<out Annotation>,
    ): Async<*> {
        returnType as ParameterizedType
        if (ApiAsync::class.java.isAssignableFrom(returnType.rawType as Class<*>)) {
            return ApiAsyncImpl(call, errorHandler)
        }
        return super.convert(call, returnType, annotations)
    }

    override fun responseTypeOf(type: Type): Type {
        return ParameterizedTypeImpl(ApiResponse::class, arrayOf(type))
    }

    private class ApiAsyncImpl(
        private val call: Call<Any>,
        private val errorHandler: ApiCallErrorHandler
    ) : ApiAsync<Any> {
        @Suppress("unchecked_cast")
        private val async = AsyncImpl(
            call as Call<ApiResponse<Any>>,
            errorHandler
        )

        override suspend fun awaitNullable(): Any? {
            val result = async.awaitNullable()
            if (result?.statusCode != null && result.statusCode >= 400) throw ApiRequestException(
                result.message
            )
            return result?.data
        }

        override suspend fun clone(): Async<Any> {
            return ApiAsyncImpl(call.clone(), errorHandler)
        }
    }
}