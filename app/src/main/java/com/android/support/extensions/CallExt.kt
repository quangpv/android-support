package com.android.support.extensions

import android.support.core.extensions.withIO
import com.android.support.app.ApiRequestException
import com.android.support.app.InternalServerException
import com.android.support.app.ParameterInvalidException
import com.android.support.app.ServerResponseNullException
import com.google.gson.Gson
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.io.File
import java.net.ConnectException
import java.util.concurrent.TimeoutException

private fun <T> Call<T>.doCall(): T? {
    val result = try {
        execute()
    } catch (e: Throwable) {
        if (e is ConnectException) {
            e.printStackTrace()
            throw Throwable("Can not connect to server, please try again.")
        }
        throw e
    }
    if (!result.isSuccessful) {
        val code = result.code()
        throw when {
            code in 400..499 -> ApiRequestException(result.errorBody()?.string())
            code >= 500 -> InternalServerException()
            else -> ParameterInvalidException(result.message())
        }
    }
    return result.body()
}

fun <T> Call<T>.syncCall(): T {
    return doCall() ?: throw ServerResponseNullException()
}

suspend fun <T> Call<T>.callNullable(): T? = withIO { doCall() }

suspend fun <T> Call<T>.call(): T {
    return callNullable() ?: throw ServerResponseNullException()
}

suspend fun <T> Call<T>.callRetry(
    timeout: Int,
    timeDelay: Int,
    accept: (T) -> Boolean
): T {
    var result: T
    var timeCount = 0
    while (true) {
        result = clone().call()
        if (accept(result)) break
        timeCount++
        if (timeCount * timeDelay >= timeout) throw TimeoutException("Retry timeout")
        delay(timeDelay * 1000L)
    }
    return result
}

fun Map<String, String>.buildParts(): Map<String, RequestBody> {
    val part = hashMapOf<String, RequestBody>()
    forEach {
        part[it.key] = createTextPart(it.value)
    }
    return part
}

fun createTextPart(value: String): RequestBody {
    return value.toRequestBody("text/plain".toMediaType())
}

fun buildImagePart(name: String, photo: String): MultipartBody.Part {
    val file = File(photo)
    return MultipartBody.Part.createFormData(
        name,
        file.name,
        file.asRequestBody("image/${file.extension}".toMediaType())
    )
}

fun buildPart(name: String, data: Any): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name,
        Gson().toJson(data)
    )
}
