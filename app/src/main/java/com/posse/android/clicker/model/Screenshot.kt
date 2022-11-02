package com.posse.android.clicker.model

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.OutputStreamWriter

class Screenshot(private val outputStream: OutputStreamWriter, private val process: Process) {

    @Suppress("BlockingMethodInNonBlockingContext")
    fun get(): Bitmap? {
        return runBlocking(Dispatchers.Default) {
            try {
                outputStream.write("/system/bin/screencap -p\n")
                outputStream.flush()
                return@runBlocking BitmapFactory.decodeStream(
                    process.inputStream,
                    null,
                    BitmapFactory.Options().apply { inMutable = true })!!
            } catch (e: IOException) {
                e.printStackTrace()
            }
            null
        }
    }

    fun getWithHole(cx: Float, cy: Float, radius: Float): Bitmap? {
        val result = get() ?: return null
        val canvas = Canvas(result)
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        canvas.drawCircle(cx, cy, radius, paint)
        return result
    }
}

enum class ScreenShotType {
    Full,
    WithHole
}