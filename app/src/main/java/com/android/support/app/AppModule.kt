package com.android.support.app

import android.app.Application
import android.support.applifecycle.ApplicationScope
import android.support.di.module
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.android.support.datasource.AuthenticateApi
import com.android.support.datasource.MockAuthenticateApi
import com.android.support.helper.network.TLSSocketFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

val appModule = module {
    modules(serializeModule, networkModule, apiModule)
    single { ApplicationScope() }
}

val serializeModule = module {
    single { Gson() }
    single<Converter.Factory> {
        GsonConverterFactory.create(
            GsonBuilder()
                .create()
        )
    }
}

val networkModule = module {
    single {
        HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    single {
        val application: Application = get()
        val cacheDir = File(application.cacheDir, UUID.randomUUID().toString())
        val cache = Cache(cacheDir, 10485760L) // 10mb
        val tlsSocketFactory = TLSSocketFactory()
        OkHttpClient.Builder()
            .sslSocketFactory(tlsSocketFactory, tlsSocketFactory.systemDefaultTrustManager())
            .cache(cache)
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    factory<Retrofit.Builder> {
        Retrofit.Builder()
            .addConverterFactory(get())
            .client(get())
    }
}

val apiModule = module {

    single<AuthenticateApi> {
        MockAuthenticateApi()
    }

//    single<ApiService> {
//        get<Retrofit.Builder>()
//            .baseUrl(AppConfig.endpoint)
//            .build()
//            .create(ApiService::class.java)
//    }
}