package com.posse.android.clicker.utils

import android.content.SharedPreferences
import com.posse.android.clicker.BuildConfig

var SharedPreferences.running: Boolean
    get() = this.getBoolean("running", false)
    set(value) {
        this.edit()
            .putBoolean("running", value)
            .apply()
    }

var SharedPreferences.lastScript: String?
    get() = this.getString("script", null)
    set(value) {
        this.edit()
            .putString("script", value)
            .apply()
    }

var SharedPreferences.windowXPosition: Int
    get() = this.getInt("xPosition", 0)
    set(value) {
        this.edit()
            .putInt("xPosition", value)
            .apply()
    }

var SharedPreferences.windowYPosition: Int
    get() = this.getInt("yPosition", 0)
    set(value) {
        this.edit()
            .putInt("yPosition", value)
            .apply()
    }

var SharedPreferences.expanded: Boolean
    get() = this.getBoolean("expanded", false)
    set(value) {
        this.edit()
            .putBoolean("expanded", value)
            .apply()
    }

var SharedPreferences.lastError: String?
    get() = this.getString("error", null)
    set(value) {
        this.edit()
            .putString("error", value)
            .apply()
    }

var SharedPreferences.chatID: Long?
    get() = this.getLong("chatID", BuildConfig.CHAT_ID.toLong())
    set(value) {
        value?.let {
            this.edit()
                .putLong("chatID", it)
                .apply()
        }
    }

var SharedPreferences.botToken: String?
    get() = this.getString("token", BuildConfig.API_TOKEN)
    set(value) {
        this.edit()
            .putString("token", value)
            .apply()
    }

var SharedPreferences.animator: Boolean
    get() = this.getBoolean("animator", true)
    set(value) {
        this.edit()
            .putBoolean("animator", value)
            .apply()
    }

var SharedPreferences.telegramMsg: String?
    get() = this.getString("telegramErrorText", null)
    set(value) {
        this.edit()
            .putString("telegramErrorText", value)
            .apply()
    }

var SharedPreferences.loginText: String?
    get() = this.getString("telegramLoginText", null)
    set(value) {
        this.edit()
            .putString("telegramLoginText", value)
            .apply()
    }