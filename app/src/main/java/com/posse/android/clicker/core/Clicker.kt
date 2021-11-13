package com.posse.android.clicker.core

import android.content.SharedPreferences
import android.graphics.Bitmap
import com.posse.android.clicker.databinding.FragmentMainBinding
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.scripts.FifaMobile
import com.posse.android.clicker.telegram.Telegram
import com.posse.android.clicker.ui.Animator
import com.posse.android.clicker.utils.animator
import com.posse.android.clicker.utils.loginText
import com.posse.android.clicker.utils.running
import com.posse.android.clicker.utils.telegramMsg
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.OutputStreamWriter

class Clicker(private val binding: FragmentMainBinding) : KoinComponent {

    private val preferences: SharedPreferences by inject()
    private val telegram: Telegram by inject()
    private val log: MyLog by inject()
    private val screenshot: Screenshot by inject()
    private val outputStream: OutputStreamWriter by inject()
    private var clickerJob: Job? = null
    private var telegramJob: Job? = null
    private val animator: Animator? = if (preferences.animator) Animator(binding.root) else null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(script: SCRIPT) {
        if (clickerJob == null) {
            preferences.running = true
            binding.startButton.setBackgroundColor(binding.root.context.getColor(android.R.color.holo_green_light))
            clickerJob = coroutineScope.launch {
                var msg = preferences.telegramMsg
                if (msg.isNullOrEmpty()) msg = " "
                var loginMsg = preferences.loginText
                if (loginMsg.isNullOrEmpty()) loginMsg = " "
                FifaMobile(this@Clicker, script, msg, loginMsg).run()
            }
        }
    }

    fun stop() {
        coroutineScope.launch { animator?.stop() }
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            binding.startButton.setBackgroundColor(
                binding.root.context.getColor(android.R.color.darker_gray)
            )
        }
        clickerJob?.cancel()
        clickerJob = null
        stopTelegram()
        preferences.running = false
    }

    fun click(x: Int, y: Int) {
        coroutineScope.launch {
            runCatching {
                outputStream.write("input tap $x $y\n")
                outputStream.flush()
            }
        }
        coroutineScope.launch {
            delay(400)
            animator?.animateClick(x, y)
        }
    }

    fun recentButton(){
        coroutineScope.launch {
            runCatching {
                outputStream.write("input keyevent KEYCODE_APP_SWITCH\n")
                outputStream.flush()
            }
        }
    }

    fun backButton(){
        coroutineScope.launch {
            runCatching {
                outputStream.write("input keyevent KEYCODE_BACK\n")
                outputStream.flush()
            }
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
            runCatching {
                outputStream.write("input swipe $startX $startY $endX $endY $duration\n")
                outputStream.flush()
            }
        }
        coroutineScope.launch {
            delay(500)
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

    fun getColor(x: Int, y: Int): Int? = getScreen()?.getPixel(x, y)

    fun getScreen() = screenshot.get()

    fun getPixelColor(screen: Bitmap, x: Int, y: Int) = screen.getPixel(x, y)

    fun putLog(message: String) = log.add(message)

    companion object {
        const val CLICK_DURATION = 1
    }
}