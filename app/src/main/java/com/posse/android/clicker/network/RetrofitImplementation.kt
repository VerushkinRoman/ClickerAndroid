package com.posse.android.clicker.network

class RetrofitImplementation(private val apiService: ApiService) {

    suspend fun sendMessage(token: String, chatID: String, text: String) {
        apiService.sendMessage(token, chatID, text)
    }
}