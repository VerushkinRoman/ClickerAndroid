package com.posse.android.clicker.di

import android.content.Context
import android.content.SharedPreferences
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.network.ApiService
import com.posse.android.clicker.network.RetrofitImplementation
import com.posse.android.clicker.telegram.Telegram
import com.posse.android.clicker.utils.botToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import java.io.OutputStreamWriter

val myLogger = module {
    single<MyLog> { MyLog() }
}

val shared = module {
    single<SharedPreferences> { androidContext().getSharedPreferences(NAME, Context.MODE_PRIVATE) }
}

val telegram = module {
    single<Telegram> { Telegram(get(), get()) }
}

val network = module {

    single<RetrofitImplementation> { RetrofitImplementation(get()) }

    single<ApiService> { get<Retrofit>().create(ApiService::class.java) }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BASE_URL + "/bot" + (get<SharedPreferences>().botToken ?: 0) + "/")
            .client(get())
            .build()
    }

    single<OkHttpClient> {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        httpClient.build()
    }
}

val text = module {
    single<TextRecognizer> { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
}

val screenshot = module {
    single<Screenshot> { Screenshot(get(), get()) }
}

val root = module {
    single<Process> { Runtime.getRuntime().exec("su") }
    single<OutputStreamWriter> { OutputStreamWriter(get<Process>().outputStream) }
}