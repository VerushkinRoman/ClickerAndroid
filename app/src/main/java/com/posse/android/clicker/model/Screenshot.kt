package com.posse.android.clicker.model

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.OutputStreamWriter

class Screenshot(private val outputStream: OutputStreamWriter, private val process: Process) {

    fun get(): Bitmap {
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
            throw RuntimeException("Screenshot error")
        }
    }

    fun getWithHole(cx: Float, cy: Float, radius: Float): Bitmap {
        val result = get()
        val canvas = Canvas(result)
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        canvas.drawCircle(cx, cy, radius, paint)
        return result
    }

    fun getWithoutPlayers(): Bitmap {
        val result = get()
        val canvas = Canvas(result)
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        canvas.drawRect(0f, 456f, 1279f, 680f, paint)
        canvas.drawRect(155f, 131f, 460f, 484f, paint)
        return result
    }
}