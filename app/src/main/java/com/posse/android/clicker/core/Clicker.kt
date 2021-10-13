package com.posse.android.clicker.core

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewConfiguration
import com.posse.android.clicker.model.MyLog
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.scripts.FifaMobile
import com.posse.android.clicker.service.MyAccessibilityService
import com.posse.android.clicker.telegram.Telegram
import com.posse.android.clicker.ui.Animator

class Clicker(view: View) {

    private var scriptThread: Thread? = null
    private var telegram: Telegram = Telegram()
    private var gestureCallback: AccessibilityService.GestureResultCallback? = null
    private val animator = Animator(view)

    fun start(script: SCRIPT) {
        if (scriptThread?.isAlive != true) {
            scriptThread = Thread(FifaMobile(this, script))
            scriptThread?.start()
        }
    }

    fun stop() {
        scriptThread?.interrupt()
        stopTelegram()
    }

    fun click(x: Int, y: Int) {
        animator.animateClick(x, y)
        Handler(Looper.getMainLooper()).postDelayed({
            dispatchGestureToService(makeGesture(Gesture.Click(x, y)))
        }, Animator.ANIMATION_DURATION)
    }

    fun drag(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long) {
        animator.animateDrag(startX, startY, endX, endY, duration)
        Handler(Looper.getMainLooper()).postDelayed({
            dispatchGestureToService(
                makeGesture(
                    Gesture.Drag(
                        startX,
                        startY,
                        endX,
                        endY,
                        duration
                    )
                )
            )
        }, Animator.ANIMATION_DURATION)
    }

    fun startTelegram(msg: String, delay: Int, repeat: Boolean) {
        if (!telegram.isAlive) {
            telegram = Telegram()
            telegram.start()
        } else telegram.countdown = 0
        telegram.msg = msg
        telegram.delay = delay
        telegram.repeat = repeat
    }

    fun stopTelegram() = telegram.interrupt()

    fun getColor(x: Int, y: Int) {
        val picture = Screenshot.get()
        val pixel = picture?.getPixel(x, y)
        MyLog.add(pixel.toString())
    }

    private fun dispatchGestureToService(gesture: GestureDescription) {
        MyAccessibilityService.instance?.dispatchGesture(
            gesture,
            gestureCallback,
            null
        )
    }

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
                duration = LONG_CLICK.toInt()
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
        val duration: Long
    ) : Gesture()
}