package com.posse.android.clicker.telegram

import com.posse.android.clicker.BuildConfig
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL
import java.net.URLConnection

data class Telegram(
    private val chatID: String = BuildConfig.CHAT_ID,
    private val apiToken: String = BuildConfig.API_TOKEN,
    var delay: Int = 0,
    var msg: String = "",
    var repeat: Boolean = false,
    var countdown: Int = 0
) : Thread() {

    @Synchronized
    override fun run() {
        do {
            try {
                countdown = 0
                while (countdown < delay) {
                    sleep(1000)
                    countdown++
                }
                val urlString = String.format(urlMask, apiToken, chatID, msg)
                val url = URL(urlString)
                val connection: URLConnection = url.openConnection()
                BufferedInputStream(connection.getInputStream())
            } catch (e: IOException) {
                currentThread().interrupt()
            } catch (e: InterruptedException) {
                currentThread().interrupt()
            }
        } while (repeat && !currentThread().isInterrupted)
    }

    companion object {
        private const val urlMask = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s"
    }
}