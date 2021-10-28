package com.posse.android.clicker.app

import android.app.Application
import com.posse.android.clicker.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(shared, myLogger, telegram, network, screenshot, root))
        }
    }
}