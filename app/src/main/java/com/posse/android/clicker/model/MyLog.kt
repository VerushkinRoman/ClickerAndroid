package com.posse.android.clicker.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyLog {

    private val log: MutableStateFlow<String> = MutableStateFlow("")

    fun get(): StateFlow<String> = log

    fun add(string: String) {
        log.value = string
    }
}