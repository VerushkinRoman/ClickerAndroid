package com.posse.android.clicker.app

import android.app.Application
import com.posse.android.clicker.di.components.AppComponent
import com.posse.android.clicker.di.components.DaggerAppComponent
import com.posse.android.clicker.di.modules.AppModule

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    companion object {

        lateinit var instance: App
    }
}