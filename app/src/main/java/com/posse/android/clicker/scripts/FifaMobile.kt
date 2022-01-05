package com.posse.android.clicker.scripts

import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.Game
import com.posse.android.clicker.core.Script
import com.posse.android.clicker.model.ScreenShotType
import com.posse.android.clicker.model.Screenshot
import com.posse.android.clicker.scripts.base.BaseScript
import com.posse.android.clicker.ui.Animator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class FifaMobile(
    clicker: Clicker,
    script: Script,
    msg: String,
    loginMsg: String
) : BaseScript(clicker, script, msg, loginMsg) {

    private var exitCycle = false

    override suspend fun run() {
        while (true) {
            super.run()
            when (script) {
                Game.Market -> marketClicking()
                Game.EventAttack -> eventAttack()
//                Game.Test -> testClicking()
            }
        }
    }

//    private suspend fun testClicking() {
//        click(400, 400)
//        pause(2000)
//    }

    private suspend fun eventAttack() {
        if (pixel(217, 126) == -2868195) {
            log("Event main")
            click(332, 113)
        }

        if (pixel(402, 143) == -367826) {
            log("Day of dead")
            if (((pixel(684, 631) == -39636) || (pixel(668, 624) == -1549272))
                && (pixel(417, 651) != -39636)
            ) {
                log("Vs attack")
                click(635, 592)
                pause(5_000)
                makeScreenshot(ScreenShotType.Full)
            }
        }

        if ((pixel(417, 651) == -39636) || (pixel(399, 631) == -2533851)) {
            log("Ready Play")
            if (pixel(902, 291) == -1749997) {
                log("Play")
                click(1019, 631)
                pause(120_000)
            }
        }

        errorsCheck()
    }


    private suspend fun marketClicking() {

        if (pixel(200, 61) == -15462074 //mail
            && ((pixel(227, 139) == -13305319) //green
                    || pixel(227, 138) == -6422528) //red
        ) {
            log("mail. Collect")
            clickAndWait(943, 134)
        }

        if (pixel(202, 57) == -44780 // mail
            && pixel(794, 390) == -1
        ) { // no letters
            log("no letters. Break")
            stop()
        }

        if (pixel(983, 425) == -7383297) {
            log("sell")
            click(1014, 425)
            pause()
            startTelegram(msg, delay, true)
        }

        if (pixel(983, 417) == -13226157) {
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
                dragAndWait(124, 564, 231, 263, 1000)
            }
        }

        if (pixel(509, 95) == -15458241) {
            log("market")
            startClickingFirstPlayer()
            if (pixel(31, 545) == -14932155) {
                log("at least 1 player")
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
        } else stopClickingFirstPlayer()

        errorsCheck()
    }

    private suspend fun startClickingFirstPlayer() {
        if (job == null) job = CoroutineScope(coroutineContext).launch {
            while (isActive) {
                click(323, 665)
                pause(500)
            }
        }
    }

    private fun stopClickingFirstPlayer() {
        job?.cancel("Stop clicking")
        job = null
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
        result.add(NUMBERS.One)
        while (result.size < capacity) {
            val number = NUMBERS.values()[(Math.random() * capacity).toInt()]
            if (!result.contains(number)) result.add(number)
        }
        return result
    }

    private suspend fun buyProcedure(x: Int) {
        log("shopping")
        clickAndWait(x, 510)
        log("player card x:$x")
        exitCycle = false
        var canNext = false
        val startTime = System.currentTimeMillis()
        while (!exitCycle) {
            if (pixel(482, 50) != -12440173) canNext = true
            connectionAndBetErrorCheck()
            openedPlayerCardCheck()
            if (canNext && pixel(482, 50) == -12440173) {
                log("exit to market")
                exitCycle = true
            }
            if (job == null || System.currentTimeMillis() > startTime + 5_000) errorsCheck()
            if (System.currentTimeMillis() > startTime + 10_000) exitCycle = true
            makeScreenshot(ScreenShotType.Full)
        }
    }

    private suspend fun connectionAndBetErrorCheck(): Boolean {
        return if (pixel(260, 236) == -44780) {
            log("connection/bet error")
            if (pixel(563, 288) == -16281669) {
                log("logged in")
                startTelegram(loginMsg, 0, false)
                pause(5_000)
                stop()
                true
            } else {
                click(272, 480)
                true
            }
        } else false
    }

    private suspend fun openedPlayerCardCheck(): Boolean {
        return if (pixel(1039, 411) == -16318562) {
            log("opened player card")
            click(34, 44)
            true
        } else false
    }

    private suspend fun errorsCheck() {

        makeScreenshot(ScreenShotType.Full)

        if (pixel(972, 669) == -7383297) {
            log("next")
            click(972, 669)
            return
        }

        if (pixel(1044, 681) == -12440173 // next
            && pixel(215, 657) != -14024759 // not buy
        ) {
            log("if next")
            click(1064, 681)
            return
        }

        if (pixel(731, 346) == -4413106 //coin
            && pixel(224, 663) == -14024759 //buy button
        ) {
            log("buy")
            click(224, 663)
            return
        }

        if (connectionAndBetErrorCheck()) return
        if (openedPlayerCardCheck()) return

        if (pixel(364, 430) == -16743049) {
            log("service error")
            click(205, 96)
            return
        }

        if (pixel(37, 286) == -12440173) {
            log("main screen")
            click(37, 286)
            exitCycle = true
            return
        }

        if (pixel(201, 93) == -44780 // popup red
            && pixel(1043, 92) == -1 // popup close
            && pixel(1064, 645) == -14024759 // popup cyan
        ) {
            log("some popup")
            click(1043, 92)
            return
        }

        if (pixel(366, 430) == -16743049) {
            log("services error")
            click(366, 430)
            return
        }

        if (pixel(272, 449) == -2842294) {
            log("league tournament")
            click(1044, 666)
            return
        }

        if (pixel(202, 71) == -1357032 && pixel(1067, 685) == -14024759) {
            log("new event. confirm")
            click(1067, 685)
            return
        }

        if (pixel(119, 128) == -44780) {
            log("daily news")
            click(1043, 57)
            return
        }

        if (pixel(244, 29) == -16335885) {
            log("shop")
            click(244, 29)
            exitCycle = true
            return
        }
    }

    private suspend fun dragAndWait(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long,
    ) {
        clicker.drag(startX, startY, endX, endY, duration)
        pause(
            duration
                    + Animator.ANIMATION_DURATION
                    + Clicker.CLICK_DURATION.toLong()
        )
        while (!makeScreenshotWithoutPlayers()) pause(100)
    }

    private fun makeScreenshotWithoutPlayers(): Boolean {
        log("screenshot without players")
        oldScreen = screen
        screen = clicker.getScreen(ScreenShotType.WithoutPlayers)
        return oldScreen.sameAs(screen)
    }

}

enum class NUMBERS(val value: Int) {
    One(1),
    Two(2),
    Three(3),
    Four(4)
}