package com.posse.android.clicker.core

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.posse.android.clicker.databinding.FragmentMainBinding
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.scripts.FifaMobile
import com.posse.android.clicker.telegram.Telegram
import com.posse.android.clicker.ui.Animator
import com.posse.android.clicker.utils.running
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.OutputStreamWriter

class Clicker(private val binding: FragmentMainBinding) : KoinComponent {

    private var clickerJob: Job? = null
    private val telegram: Telegram by inject()
    private var telegramJob: Job? = null
    private val animator = Animator(binding.root)
    private val log: MyLog by inject()
    private val screenshot: Screenshot by inject()
    private val preferences: SharedPreferences by inject()
    private val outputStream: OutputStreamWriter by inject()

    fun start(script: SCRIPT) {
        if (clickerJob == null) {
            preferences.running = true
            binding.startButton.setBackgroundColor(binding.root.context.getColor(android.R.color.holo_green_light))
            clickerJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                FifaMobile(this@Clicker, script).run()
            }
        }
    }

    fun stop() {
        binding.startButton.setBackgroundColor(binding.root.context.getColor(android.R.color.darker_gray))
        clickerJob?.cancel()
        clickerJob = null
        stopTelegram()
        animator.stop()
        preferences.running = false
    }

    fun click(x: Int, y: Int) {
        animator.animateClick(x, y)
        Handler(Looper.getMainLooper()).postDelayed({
            outputStream.write("input tap $x $y\n")
            outputStream.flush()
        }, Animator.ANIMATION_DURATION)
    }

    fun drag(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long
    ) {
        animator.animateDrag(startX, startY, endX, endY, duration)
        Handler(Looper.getMainLooper()).postDelayed({
            outputStream.write("input swipe $startX $startY $endX $endY $duration\n")
            outputStream.flush()
        }, Animator.ANIMATION_DURATION)
    }

    fun startTelegram(msg: String, delay: Int, repeat: Boolean) {
        if (telegramJob == null) {
            telegramJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                telegram.run()
            }
        } else telegram.countdown = 0
        telegram.msg = msg
        telegram.delay = delay
        telegram.repeat = repeat
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