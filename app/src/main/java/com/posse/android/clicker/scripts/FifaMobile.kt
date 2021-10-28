package com.posse.android.clicker.scripts

import android.graphics.Bitmap
import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.SCRIPT
import com.posse.android.clicker.ui.Animator
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.ZoneId

class FifaMobile(private val clicker: Clicker, private val script: SCRIPT) {

    private val delay = 300
    private val msg = "Вылет!"
    private val startQuietTime = LocalTime.of(22, 0)
    private val endQuietTime = LocalTime.of(8, 0)
    private val zoneId: ZoneId = ZoneId.of("Europe/Moscow")
    private var exitCycle = false
    private lateinit var now: LocalTime
    private lateinit var screen: Bitmap

    suspend fun run() {
        while (true) {

            screen = getScreen()

            when (script) {
                SCRIPT.Market -> marketClicking()
                SCRIPT.Test -> test()
            }

            now = LocalTime.now(zoneId)
            if (now.isAfter(startQuietTime) || now.isBefore(endQuietTime)) {
                clicker.stopTelegram()
            }
        }
    }

    private suspend fun test() {
        click(200, 200)
        pause()
    }

    private suspend fun marketClicking() {

        if (pixel(200, 61) == -15462074 //mail
            && (pixel(227, 139) == -13305319) //green
        ) {
            log("mail. Collect")
            click(943, 134)
        }

        if (pixel(202, 57) == -44780 // mail
            && pixel(794, 390) == -1
        ) { // no letters
            log("no letters. Break")
            stop()
        }

        if (pixel(1014, 425) == -7383297) {
            log("sell")
            click(1014, 425)
            pause()
            startTelegram(msg, delay, true)
        }

        if (pixel(988, 417) == -13226157) {
            log("didn't sold")
            if (pixel(70, 586) == -14932155 // no first player
                && pixel(194, 581) == -14932155 // no second player
            ) {
                log("no players for sell")
                click(34, 48)
                pause(2000)
                click(199, 94)
                pause(2000)
            } else if (pixel(270, 246) == -14932155) {
                log("no card. Dragging")
                drag(124, 564, 231, 263, 1000)
            }
        }

        if (pixel(509, 95) == -15458241) {
            log("market")
            if (pixel(31, 545) == -14932155) {
                log("at least 1 player")
                pause(300)
                buy()
                click(477, 37)
                log("refresh")
                startTelegram(msg, delay, true)
            } else if (pixel(726, 377) == -16250348) {
                log("not found")
                click(477, 37)
                log("refresh")
                startTelegram(msg, delay, true)
            }
        }

        pause()

        errorsCheck()
    }

    private suspend fun buy() {
        var cardsCount = 1
        for (i in 1 until NUMBERS.values().size) {
            if (pixel(i * 345 + 35, 554) == -14932155) {
                cardsCount++
            }
        }
        val numbers: ArrayList<NUMBERS> = getRandomNumbers(cardsCount)
        for (number in numbers) {
            val xCard = number.value * 300
            val xMy: Int
            val xEnemy: Int
            when (number) {
                NUMBERS.One -> {
                    xMy = 59
                    xEnemy = 36
                }
                NUMBERS.Two -> {
                    xMy = 403
                    xEnemy = 380
                }
                NUMBERS.Three -> {
                    xMy = 747
                    xEnemy = 723
                }
                NUMBERS.Four -> {
                    xMy = 1092
                    xEnemy = 1069
                }
            }
            if (pixel(xMy, 508) != -13840292 &&
                pixel(xEnemy, 500) != -3394765
            ) {
                buyProcedure(xCard)
            }
        }
    }

    private fun getRandomNumbers(capacity: Int): ArrayList<NUMBERS> {
        val result: ArrayList<NUMBERS> = ArrayList(capacity)
        while (result.size < capacity) {
            val number = NUMBERS.values()[(Math.random() * capacity).toInt()]
            if (!result.contains(number)) result.add(number)
        }
        return result
    }

    private suspend fun buyProcedure(x: Int) {
        log("shopping")
        click(x, 510)
        log("player card x:$x")
        pause()
        screen = getScreen()
        exitCycle = pixel(482, 50) == -12440173
        while (!exitCycle) {
            if (pixel(215, 657) == -14024759) {
                clicker.putLog("buy")
                click(381, 664)
            }
            pause()
            screen = getScreen()
            errorsCheck()
            if (pixel(482, 50) == -12440173) {
                log("exit to market")
                exitCycle = true
            }
        }
    }

    private suspend fun errorsCheck() {

        if (pixel(364, 430) == -16743049) {
            log("service error")
            click(205, 96)
        }

        if (pixel(37, 286) == -12440173) {
            log("main screen")
            click(37, 286)
            exitCycle = true
            pause()
        }

        if (pixel(201, 93) == -44780 // popup red
            && pixel(1043, 92) == -1 // popup close
            && pixel(1064, 645) == -14024759 // popup cyan
        ) {
            log("some popup")
            click(1043, 92)
        }

        if (pixel(366, 430) == -16743049) {
            log("services error")
            click(366, 430)
        }

        if (pixel(972, 669) == -7383297) {
            log("next")
            click(972, 669)
        }

        if (pixel(244, 29) == -16335885) {
            log("shop")
            click(244, 29)
            exitCycle = true
        }

        if (pixel(1064, 681) == -12440173 // next
            && pixel(215, 657) != -14024759 // not buy
        ) {
            log("if next")
            click(1064, 681)
            screen = getScreen()
        }

        if (pixel(119, 128) == -44780) {
            log("daily news")
            click(1043, 57)
            pause()
        }

        if (pixel(260, 236) == -44780) {
            log("connection/bet error")
            pause()
            screen = getScreen()
            if (pixel(838, 439) == -16088404) { // todo
                log("logged in")
                startTelegram("Я зашел", 0, false)
                pause(3_000)
                stop()
            } else {
                click(272, 480)
                pause()
                exitCycle = true
            }
        }

        if (pixel(202, 71) == -1357032 && pixel(1067, 685) == -14024759) {
            log("new event. confirm")
            click(1067, 685)
        }

        if (pixel(1039, 411) == -16318562) {
            log("opened player card")
            click(34, 44)
        }

        if (pixel(272, 449) == -2842294) {
            log("league tournament")
            click(1044, 666)
        }

    }

    private suspend fun click(x: Int, y: Int) {
        clicker.click(x, y)
        pause(Animator.ANIMATION_DURATION)
    }

    private suspend fun drag(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long,
        useLongClick: Boolean = true
    ) {
        clicker.drag(startX, startY, endX, endY, duration, useLongClick)
        pause(
            duration
                    + Animator.ANIMATION_DURATION
                    + if (useLongClick) Clicker.LONG_CLICK else Clicker.CLICK_DURATION.toLong()
        )
    }

    private suspend fun pause(duration: Long = 1000) = delay(duration)

    private fun log(message: String) = clicker.putLog(message)

    private fun getScreen(): Bitmap {
        log("screenshot")
        return clicker.getScreen()!!
    }

    private fun stop() = clicker.stop()

    private fun pixel(x: Int, y: Int) = clicker.getPixelColor(screen, x, y)

    private fun startTelegram(msg: String, delay: Int, repeat: Boolean) {
        now = LocalTime.now(zoneId)
        if (now.isAfter(startQuietTime) || now.isBefore(endQuietTime)) {
            clicker.stopTelegram()
        } else {
            clicker.startTelegram(msg, delay, repeat)
        }
    }
}

enum class NUMBERS(val value: Int) {
    One(1),
    Two(2),
    Three(3),
    Four(4)
}