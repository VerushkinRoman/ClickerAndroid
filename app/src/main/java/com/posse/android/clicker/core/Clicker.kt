package com.posse.android.clicker.core

import android.graphics.Bitmap
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.scripts.FifaMobile
import com.posse.android.clicker.scripts.LooneyTunes
import com.posse.android.clicker.telegram.Telegram
import com.posse.android.clicker.ui.Animator
import com.posse.android.clicker.ui.MainFragment
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.io.OutputStreamWriter

class Clicker(
    private val msg: String,
    private val loginMsg: String,
    private val animator: Animator?,
    private val log: MyLog,
    private val screenshot: Screenshot,
    private val startButtonChanger: MainFragment.StartButtonChanger
) : KoinComponent {

    private val telegram: Telegram by inject()
    private val outputStream: OutputStreamWriter by inject()
    private var clickerJob: Job? = null
    private var telegramJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var x: Int = 0
    private var y: Int = 0

    fun start(script: Script) {
        if (clickerJob == null) {
            startButtonChanger.changeColor(true)
            clickerJob = coroutineScope.launch {
                when (script.game) {
                    Games.FifaMobile -> FifaMobile(this@Clicker, script, msg, loginMsg).run()
                    Games.LooneyTunes -> LooneyTunes(this@Clicker, script, msg, loginMsg).run()
                }
            }
        }
    }

    fun stop() {
        coroutineScope.launch { animator?.stop() }
        coroutineScope.launch { startButtonChanger.changeColor(false) }
        clickerJob?.cancel()
        clickerJob = null
        stopTelegram()
    }

    fun click(x: Int, y: Int) {
        this.x = x
        this.y = y
        coroutineScope.launch {
            sendCommand("input tap $x $y\n")
            delay(500)
            animator?.animateClick(x, y)
        }
    }

    fun recentButton() = sendCommand("input keyevent KEYCODE_APP_SWITCH\n")

    fun backButton() = sendCommand("input keyevent KEYCODE_BACK\n")

    private fun sendCommand(command: String) {
        try {
            outputStream.write(command)
            outputStream.flush()
        } catch (e: IOException) {
            log.add(e.toString())
        }
    }

    fun drag(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long
    ) {
        coroutineScope.launch {
            sendCommand("input swipe $startX $startY $endX $endY $duration\n")
            delay(400)
            animator?.animateDrag(startX, startY, endX, endY, duration)
        }
    }

    fun startTelegram(msg: String, delay: Int, repeat: Boolean) {
        telegram.msg = msg
        telegram.delay = delay
        telegram.repeat = repeat
        if (telegramJob == null) {
            telegramJob = coroutineScope.launch {
                telegram.run()
            }
        } else telegram.countdown = 0
    }

    fun stopTelegram() {
        telegramJob?.cancel()
        telegramJob = null
    }

    fun getScreen(): Bitmap {
        return if (animator == null) screenshot.get()
        else screenshot.getWithHole(
            x.toFloat(),
            y.toFloat(),
            Animator.SIZE * Animator.MAX_SCALE * .5f
        )
    }

    fun getPixelColor(screen: Bitmap, x: Int, y: Int) = screen.getPixel(x, y)

    fun putLog(message: String) = log.add(message)

    companion object {
        const val CLICK_DURATION = 1
    }
}