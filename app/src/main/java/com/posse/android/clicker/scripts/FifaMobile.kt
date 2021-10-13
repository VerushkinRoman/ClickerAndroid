package com.posse.android.clicker.scripts

import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.SCRIPT

class FifaMobile(private val clicker: Clicker, private val script: SCRIPT) : Runnable {
    override fun run() {
        try {
            while (!Thread.currentThread().isInterrupted) {
                when (script) {
                    SCRIPT.Market -> marketClicking()
                }
            }
        } catch (e: InterruptedException) {
        }
    }

    private fun marketClicking() {
//        click(50, 150)
        drag(200, 200, 500, 500, 1500)
//        getColor(200, 300)
        pause(10_000)
//        click(150, 150)
//        pause(10_000)
    }

    private fun click(x: Int, y: Int) = clicker.click(x, y)

    private fun drag(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long) {
        clicker.drag(startX, startY, endX, endY, duration)
        pause(duration + Clicker.LONG_CLICK)
    }

    private fun pause(duration: Long) = Thread.sleep(duration)

    private fun getColor(x: Int, y: Int) = clicker.getColor(x, y)
}