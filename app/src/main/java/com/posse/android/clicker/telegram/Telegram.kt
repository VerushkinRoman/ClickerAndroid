package com.posse.android.clicker.telegram

import com.posse.android.clicker.BuildConfig
import com.posse.android.clicker.network.RetrofitImplementation
import kotlinx.coroutines.delay

data class Telegram(
    private val retrofit: RetrofitImplementation,
    private val chatID: String = BuildConfig.CHAT_ID,
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
            retrofit.sendMessage(chatID.toLong(), msg)
        } while (repeat)
    }
}