package com.posse.android.clicker.app

import android.app.Application
import android.content.SharedPreferences
import com.posse.android.clicker.di.*
import com.posse.android.clicker.utils.lastError
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import kotlin.system.exitProcess

class App : Application() {

    private val prefs: SharedPreferences by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(shared, myLogger, telegram, network, screenshot, root, text))
        }

        Thread.setDefaultUncaughtExceptionHandler { _, e -> handleUncaughtException(e) }
    }

    private fun handleUncaughtException(e: Throwable) {
        prefs.lastError = e.message.toString()
        e.printStackTrace()
        exitProcess(1)
    }
}