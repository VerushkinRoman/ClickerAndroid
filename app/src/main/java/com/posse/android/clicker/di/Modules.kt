package com.posse.android.clicker.di

import android.content.Context
import android.content.SharedPreferences
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.network.ApiService
import com.posse.android.clicker.network.RetrofitImplementation
import com.posse.android.clicker.telegram.Telegram
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit

val myLogger = module {
    single<MyLog> { MyLog() }
}

val shared = module {
    single<SharedPreferences> { androidContext().getSharedPreferences(NAME, Context.MODE_PRIVATE) }
}

val telegram = module {
    single<Telegram> { Telegram(get()) }
}

val network = module {

    single<RetrofitImplementation> { RetrofitImplementation(get()) }

    single<ApiService> { get<Retrofit>().create(ApiService::class.java) }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .build()
    }

    single<OkHttpClient> {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        httpClient.build()
    }
}

val screenshot = module {
    single<Screenshot> { Screenshot(get()) }
}

val root = module {
    single<Process> { Runtime.getRuntime().exec("su") }
}