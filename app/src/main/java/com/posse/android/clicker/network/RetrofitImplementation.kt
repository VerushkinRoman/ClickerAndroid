package com.posse.android.clicker.network

class RetrofitImplementation(private val apiService: ApiService) {

    suspend fun sendMessage(chatID: Long, text: String) {
        apiService.sendMessage(chatID, text)
    }
}