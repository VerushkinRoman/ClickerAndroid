package com.posse.android.clicker.di.components

import com.posse.android.clicker.di.modules.AppModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
    ]
)
interface AppComponent {

}