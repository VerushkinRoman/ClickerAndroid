package com.posse.android.clicker.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.PopupWindow
import com.posse.android.clicker.core.Clicker
import kotlinx.coroutines.*

class Animator(private val rootView: View) {

    private val windows = arrayListOf<PopupWindow>()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private fun getCircle() = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setSize(SIZE, SIZE)
        setColor(Color.WHITE)
    }

    private fun getImage() = ImageView(rootView.context).apply {
        background = null
        alpha = 0f
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        setImageDrawable(getCircle())
        scaleX = MIN_SCALE
        scaleY = scaleX
    }

    private fun getPopupWindow() = (
            windows.find { !it.isShowing }
                ?: PopupWindow().apply {
                    height = (SIZE * MAX_SCALE).toInt()
                    width = height
                    contentView = getImage()
                    isClippingEnabled = false
                    isTouchable = false
                }
                    .also { windows.add(it) }
            )
        .apply { update() }

    fun animateClick(x: Int, y: Int) {
        scope.launch {
            animateFadeIn(x, y) { view, popup ->
                animateFadeOut(view, popup)
            }
        }
    }

    fun stop() {
        scope.coroutineContext.cancelChildren()
        scope.launch {
            windows.forEach {
                if (it.isShowing) it.dismiss()
            }
        }
    }

    private suspend fun animateFadeIn(
        x: Int,
        y: Int,
        callback: (view: View, popup: PopupWindow) -> Unit
    ) = coroutineScope {
        val fragmentLocation = IntArray(2)
        rootView.getLocationOnScreen(fragmentLocation)
        val popup = getPopupWindow()
        popup.showAtLocation(
            rootView,
            Gravity.NO_GRAVITY,
            -fragmentLocation[0] - popup.width / 2 + x,
            -fragmentLocation[1] - popup.height / 2 + y
        )
        val view = popup.contentView
        view
            .animate()
            .setDuration(ANIMATION_DURATION)
            .alpha(1f)
            .scaleX(MAX_SCALE)
            .scaleY(MAX_SCALE)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                callback(view, popup)
            }
    }

    private fun animateFadeOut(view: View, popup: PopupWindow) {
        view
            .animate()
            .setDuration(ANIMATION_DURATION)
            .alpha(0f)
            .scaleX(MIN_SCALE)
            .scaleY(MIN_SCALE)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                popup.dismiss()
            }
    }

    fun animateDrag(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long
    ) {
        scope.launch {
            animateFadeIn(startX, startY) { view, popup ->
                scope.launch {
                    animateMotion(duration, popup, endX, startX, endY, startY)
                    animateFadeOut(view, popup)
                }
            }
        }
    }

    private suspend fun animateMotion(
        duration: Long,
        popup: PopupWindow,
        endX: Int,
        startX: Int,
        endY: Int,
        startY: Int
    ) {
        delay(Clicker.CLICK_DURATION.toLong())
        val viewLocation = IntArray(2)
        val interval: Float = 1000 / 60f
        val steps: Float = duration.toFloat() / interval
        for (i in 0..steps.toInt()) {
            rootView.getLocationOnScreen(viewLocation)
            popup.update(
                (-viewLocation[0] + (endX - startX) / steps * i - popup.width / 2 + startX).toInt(),
                (-viewLocation[1] + (endY - startY) / steps * i - popup.height / 2 + startY).toInt(),
                -1,
                -1
            )
            delay(interval.toLong())
        }
    }

    companion object {
        const val MAX_SCALE = 3f
        const val MIN_SCALE = 0.33f
        const val SIZE = 10
        const val ANIMATION_DURATION: Long = 200
    }
}