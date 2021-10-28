package com.posse.android.clicker.utils

import android.content.SharedPreferences

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