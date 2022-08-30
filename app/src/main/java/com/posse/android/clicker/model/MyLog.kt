package com.posse.android.clicker.model

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class MyLog {

    private val log = Channel<String>()

    fun get() = log.receiveAsFlow()

    suspend fun add(string: String) {
        Log.d("Clicker", string)
        log.send(string)
    }
}