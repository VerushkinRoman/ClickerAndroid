package com.posse.android.clicker.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BackgroundView(context: Context) : FrameLayout(context) {

    private val data: MutableStateFlow<Point> = MutableStateFlow(Point(0, 0))

    fun getData(): StateFlow<Point> = data

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            data.value = Point(event.rawX.toInt(), event.rawY.toInt())
        }
        return true
    }

    data class Point(val x: Int, val y: Int)
}