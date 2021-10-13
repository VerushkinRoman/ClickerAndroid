package com.posse.android.clicker.utils

import android.content.Context
import android.widget.Toast

private var toast: Toast? = null

fun Context.showToast(message: CharSequence?) {
    message?.let {
        toast?.cancel()
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT).apply { show() }
    }
}