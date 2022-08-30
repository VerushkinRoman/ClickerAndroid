package com.posse.android.clicker.scripts.base

import android.graphics.Bitmap
import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.model.ScreenShotType
import kotlinx.coroutines.*
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

abstract class BaseScript(
    protected val clicker: Clicker,
    protected val msg: String,
    protected val loginMsg: String
) {

    protected val delay = 600
    protected open val startQuietTime: LocalTime = LocalTime.of(22, 0)
    protected open val endQuietTime: LocalTime = LocalTime.of(8, 0)
    private val zoneId: ZoneId = ZoneId.of("+3")
    private var now: LocalTime = LocalTime.now(zoneId)
    private var screen: Bitmap? = clicker.getScreen(ScreenShotType.Full)
    private var oldScreen: Bitmap? = screen
    protected val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    protected var job: Job? = null

    open suspend fun run() {
        delay(1)
        makeScreenshot(ScreenShotType.Full)
        now = LocalTime.now(zoneId)
        if (now.isAfter(startQuietTime) || now.isBefore(endQuietTime)) {
            clicker.stopTelegram()
        }
    }

    protected suspend fun click(x: Int, y: Int) {
        clicker.click(x, y)
        log("click x:$x y:$y")
    }

    protected suspend fun clickAndWait(x: Int, y: Int) {
        makeScreenshot(ScreenShotType.WithHole)
        do {
            click(x, y)
        } while (makeScreenshot(ScreenShotType.WithHole))
    }

    protected suspend fun drag(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long,
    ) {
        clicker.drag(startX, startY, endX, endY, duration)
    }

    protected fun log(message: String) = clicker.putLog(message)

    protected fun makeScreenshot(screenShotType: ScreenShotType = ScreenShotType.Full): Boolean {
        log("screenshot")
        oldScreen = screen
        screen = clicker.getScreen(screenShotType)
        return screen?.let {
            return@let oldScreen?.sameAs(it)
        } ?: false
    }

    protected fun stop() {
        clicker.stop()
    }

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