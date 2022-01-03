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

    protected val delay = 600
    protected open val startQuietTime: LocalTime = LocalTime.of(22, 0)
    protected open val endQuietTime: LocalTime = LocalTime.of(8, 0)
    private val zoneId: ZoneId = ZoneId.of("+3")
    protected var exitCycle = false
    private var now: LocalTime = LocalTime.now(zoneId)
    private var screen: Bitmap = clicker.getScreen()
    private var oldScreen: Bitmap = screen
    protected val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    protected var job: Job? = null
    private var lastTimeChecked: Long = 0

    open suspend fun run() {
        pause(1)
        makeScreenshot()
        now = LocalTime.now(zoneId)
        if (now.isAfter(startQuietTime) || now.isBefore(endQuietTime)) {
            clicker.stopTelegram()
        }
    }

    protected suspend fun click(x: Int, y: Int) {
        clicker.click(x, y)
        pause(Animator.ANIMATION_DURATION)
        log("click x:$x y:$y")
    }

    protected suspend fun clickAndWait(x: Int, y: Int) {
        makeScreenshot()
        do {
            click(x, y)
        } while (makeScreenshot())
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

    protected suspend fun dragAndWait(
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
        while (!makeScreenshot()) pause(100)
    }

    protected suspend fun pause(duration: Long = 500) = delay(duration)

    protected fun log(message: String) = clicker.putLog(message)

    protected fun makeScreenshot(): Boolean {
        log("screenshot")
        oldScreen = screen
        screen = clicker.getScreen()
        return oldScreen.sameAs(screen)
    }

    protected fun minutePassed(): Boolean {
        return if (System.currentTimeMillis() > lastTimeChecked + MINUTE) {
            lastTimeChecked = System.currentTimeMillis()
            true
        } else false
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

    companion object {
        private const val MINUTE = 60_000L
    }

}