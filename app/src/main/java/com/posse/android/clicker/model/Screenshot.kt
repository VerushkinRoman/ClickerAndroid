package com.posse.android.clicker.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.OutputStreamWriter

object Screenshot {

    private val process = Runtime.getRuntime().exec("su")
    private val outputStream = OutputStreamWriter(process.outputStream)

    @Synchronized
    fun get(): Bitmap? {
        try {
            outputStream.write("/system/bin/screencap -p\n")
            outputStream.flush()
            return BitmapFactory.decodeStream(process.inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}