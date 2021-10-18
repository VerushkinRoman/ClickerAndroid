package com.posse.android.clicker.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class BackgroundView(context: Context) : FrameLayout(context) {

    private val data = PublishSubject.create<Point>()

    fun getData(): Observable<Point> = data

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            data.onNext(Point(event.rawX.toInt(), event.rawY.toInt()))
        }
        return true
    }

    data class Point(val x: Int, val y: Int)
}