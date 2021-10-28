package com.posse.android.clicker.core

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.ViewConfiguration
import com.posse.android.clicker.databinding.FragmentMainBinding
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.scripts.FifaMobile
import com.posse.android.clicker.service.MyAccessibilityService
import com.posse.android.clicker.telegram.Telegram
import com.posse.android.clicker.ui.Animator
import com.posse.android.clicker.utils.running
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Clicker(private val binding: FragmentMainBinding) : KoinComponent {

    private var clickerJob: Job? = null
    private val telegram: Telegram by inject()
    private var telegramJob: Job? = null
    private var gestureCallback: AccessibilityService.GestureResultCallback? = null
    private val animator = Animator(binding.root)
    private val log: MyLog by inject()
    private val screenshot: Screenshot by inject()
    private val preferences: SharedPreferences by inject()

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
            dispatchGestureToService(makeGesture(Gesture.Click(x, y)))
        }, Animator.ANIMATION_DURATION)
    }

    fun drag(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long,
        useLongClick: Boolean
    ) {
        animator.animateDrag(startX, startY, endX, endY, duration, useLongClick)
        Handler(Looper.getMainLooper()).postDelayed({
            dispatchGestureToService(
                makeGesture(
                    Gesture.Drag(
                        startX,
                        startY,
                        endX,
                        endY,
                        duration,
                        useLongClick
                    )
                )
            )
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

    private fun dispatchGestureToService(gesture: GestureDescription) {
        MyAccessibilityService.instance?.dispatchGesture(
            gesture,
            gestureCallback,
            null
        )
    }

    fun putLog(message: String) = log.add(message)

    private fun makeGesture(gesture: Gesture): GestureDescription {
        val clickPath = Path()
        val dragPath = Path()
        val duration: Int
        val dragDuration: Long
        val willContinue: Boolean
        val point: Pair<Int, Int> = when (gesture) {
            is Gesture.Click -> {
                willContinue = false
                duration = CLICK_DURATION
                dragDuration = 0
                Pair(gesture.x, gesture.y)
            }
            is Gesture.Drag -> {
                willContinue = true
                duration = if (gesture.useLongClick) LONG_CLICK.toInt() else CLICK_DURATION
                dragDuration = gesture.duration
                dragPath.moveTo(gesture.startX.toFloat(), gesture.startY.toFloat())
                dragPath.lineTo(gesture.endX.toFloat(), gesture.endY.toFloat())
                Pair(gesture.startX, gesture.startY)
            }
        }
        clickPath.moveTo(point.first.toFloat(), point.second.toFloat())

        val clickStroke =
            GestureDescription.StrokeDescription(clickPath, 0, duration.toLong(), willContinue)
        if (willContinue) {
            val dragStroke = clickStroke.continueStroke(
                dragPath,
                duration.toLong(),
                dragDuration,
                !willContinue
            )
            gestureCallback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    gestureCallback = null
                    dispatchGestureToService(
                        GestureDescription.Builder().addStroke(dragStroke).build()
                    )
                }
            }
        }
        return GestureDescription.Builder().addStroke(clickStroke).build()
    }

    companion object {
        const val CLICK_DURATION = 1
        val LONG_CLICK: Long = ViewConfiguration.getLongPressTimeout() + 500L
    }
}

sealed class Gesture {
    data class Click(val x: Int, val y: Int) : Gesture()
    data class Drag(
        val startX: Int,
        val startY: Int,
        val endX: Int,
        val endY: Int,
        val duration: Long,
        val useLongClick: Boolean
    ) : Gesture()
}