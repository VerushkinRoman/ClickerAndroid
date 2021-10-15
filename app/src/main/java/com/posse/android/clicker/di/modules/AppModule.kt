package com.posse.android.clicker.di.modules

import android.content.Context
import com.posse.android.clicker.app.App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val app: App) {

    @Singleton
    @Provides
    fun app(): Context {
        return app
    }
}