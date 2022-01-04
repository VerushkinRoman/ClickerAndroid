package com.posse.android.clicker.ui

import android.graphics.Color
import android.graphics.drawable.Drawable
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

    private var isInterrupted = false

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private fun getCircle(): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setSize(SIZE, SIZE)
        drawable.setColor(Color.WHITE)
        return drawable
    }

    private fun getImage(): ImageView {
        val circle = getCircle()
        val image = ImageView(rootView.context)
        image.background = null
        image.alpha = 0f
        image.scaleType = ImageView.ScaleType.CENTER_INSIDE
        image.setImageDrawable(circle)
        image.scaleX = MIN_SCALE
        image.scaleY = image.scaleX
        return image
    }

    private fun getPopupWindow(): PopupWindow {
        var popup: PopupWindow? = null
        for (window in windows) {
            if (!window.isShowing) popup = window
        }
        if (popup == null) {
            val imageView = getImage()
            popup = PopupWindow()
            popup.height = (SIZE * MAX_SCALE).toInt()
            popup.width = popup.height
            popup.contentView = imageView
            popup.isClippingEnabled = false
            popup.isTouchable = false
            windows.add(popup)
        }
        popup.update()
        return popup
    }

    fun animateClick(x: Int, y: Int) {
        scope.launch {
            animateFadeIn(x, y) { view, popup ->
                animateFadeOut(view, popup)
            }
        }
    }

    fun stop() {
        isInterrupted = true
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
            isInterrupted = false
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
            if (isInterrupted) break
        }
    }

    companion object {
        const val MAX_SCALE = 3f
        const val MIN_SCALE = 0.33f
        const val SIZE = 10
        const val ANIMATION_DURATION: Long = 200
    }
}