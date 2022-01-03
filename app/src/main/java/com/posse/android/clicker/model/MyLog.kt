package com.posse.android.clicker.model

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyLog {

    private val log: MutableStateFlow<String> = MutableStateFlow("")

    fun get(): StateFlow<String> = log

    fun add(string: String) {
        Log.d("Clicker", string)
        log.value = string
    }
}