package com.posse.android.clicker.telegram

import android.content.SharedPreferences
import com.posse.android.clicker.network.RetrofitImplementation
import com.posse.android.clicker.utils.chatID
import kotlinx.coroutines.delay

data class Telegram(
    private val preferences: SharedPreferences,
    private val retrofit: RetrofitImplementation,
    private val chatID: Long = preferences.chatID ?: 0,
    var delay: Int = 0,
    var msg: String = "",
    var repeat: Boolean = false,
    var countdown: Int = 0
) {

    suspend fun run() {
        do {
            countdown = 0
            while (countdown < delay) {
                delay(1000)
                countdown++
            }
            retrofit.sendMessage(chatID, msg)
        } while (repeat)
    }
}