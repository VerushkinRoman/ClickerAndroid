package com.posse.android.clicker.scripts.base

import android.graphics.Bitmap
import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.Script
import com.posse.android.clicker.ui.Animator
import kotlinx.coroutines.*
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

abstract class BaseScript(
    protected val clicker: Clicker,
    protected val script: Script,
    protected val msg: String,
    protected val loginMsg: String
) {

    protected val delay = 3600
    protected open val startQuietTime: LocalTime = LocalTime.of(22, 0)
    protected open val endQuietTime: LocalTime = LocalTime.of(8, 0)
    protected val zoneId: ZoneId = ZoneId.of("+3")
    protected var exitCycle = false
    protected var now: LocalTime = LocalTime.now(zoneId)
    protected lateinit var screen: Bitmap

    protected val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    protected var job: Job? = null

    open suspend fun run() {
        screen = getScreen()
        now = LocalTime.now(zoneId)
        if (now.isAfter(startQuietTime) || now.isBefore(endQuietTime)) {
            clicker.stopTelegram()
        }
    }

    protected suspend fun click(x: Int, y: Int) {
        clicker.click(x, y)
        pause(Animator.ANIMATION_DURATION)
    }

    protected suspend fun drag(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long,
    ) {
        clicker.drag(startX, startY, endX, endY, duration)
        pause(
            duration
                    + Animator.ANIMATION_DURATION
                    + Clicker.CLICK_DURATION.toLong()
        )
    }

    protected suspend fun pause(duration: Long = 1000) = delay(duration)

    protected fun log(message: String) = clicker.putLog(message)

    @JvmName("getScreen1")
    protected fun getScreen(): Bitmap {
        log("screenshot")
        return clicker.getScreen()!!
    }

    protected fun stop() = clicker.stop()

    protected fun pixel(x: Int, y: Int) = clicker.getPixelColor(screen, x, y)

    protected fun startTelegram(msg: String, delay: Int, repeat: Boolean) {
        now = LocalTime.now(zoneId)
        if (now.isAfter(startQuietTime) || now.isBefore(endQuietTime)) {
            clicker.stopTelegram()
        } else {
            clicker.startTelegram(msg, delay, repeat)
        }
    }

}