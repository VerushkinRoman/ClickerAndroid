package com.posse.android.clicker.scripts

import android.graphics.Bitmap
import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.SCRIPT
import com.posse.android.clicker.ui.Animator
import kotlinx.coroutines.*
import okhttp3.internal.wait
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import kotlin.coroutines.coroutineContext

class FifaMobile(
    private val clicker: Clicker,
    private val script: SCRIPT,
    private val msg: String,
    private val loginMsg: String
) {

    private val delay = 3600
    private val startQuietTime = LocalTime.of(23, 59)
    private val endQuietTime = LocalTime.of(8, 0)
    private val zoneId: ZoneId = ZoneId.of("+3")
    private var exitCycle = false
    private lateinit var now: LocalTime
    private lateinit var screen: Bitmap

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    suspend fun run() {
        while (true) {

            screen = getScreen()

            when (script) {
//                SCRIPT.Market -> marketClicking()
//                SCRIPT.EventAttack -> eventAttack()
                SCRIPT.LooneyTunes -> looneyClicking()
            }

            now = LocalTime.now(zoneId)
            if (now.isAfter(startQuietTime) || now.isBefore(endQuietTime)) {
                clicker.stopTelegram()
            }
        }
    }

    private suspend fun looneyClicking() {
        var clicked = false

        if (pixel(320, 98) == -16719617) {
            log("Main Screen")
            click(181, 216)
            clicked = true
            job?.cancel()
            job = null
            startTelegram(msg, delay, true)
            pause(120_000)
        }

        if (pixel(1240, 38) == -837607) {
            log("Special Promotion")
            click(1240, 38)
            clicked = true
            pause(2_000)
        }

        if (pixel(1127, 93) == -969955) {
            log("Special Promotion small window")
            click(1127, 93)
            clicked = true
            pause(2_000)
        }

        if ((pixel(740, 367) == -1727443)
            || (pixel(732, 368) == -1727443)
            || (pixel(718, 373) == -1727443)
        ) {
            log("End of screen. watch")
            click(688, 210)
            clicked = true
            pause(2_000)
            screen = getScreen()
        }

        if ((pixel(637, 276) == -14629121) && (pixel(640, 535) == -15045)) {
            log("Watch video")
            click(642, 528)
            clicked = true
            startTelegram(msg, delay, true)
            job?.cancel()
            job = scope.launch {
                pause(180_000)
                clicker.recentButton()
                pause(3_000)
                click(930, 651)
                pause(3_000)
                click(932, 210)
                pause(2_000)
                click(55, 587)
                pause(2_000)
                job = null
            }
            pause(50_000)
            clicker.backButton()
            pause(2_000)
            screen = getScreen()
        }

        if ((pixel(37, 48) == -513) && (pixel(47, 46) == -11711152)) {  // Left corner
            log("Close")
            click(47, 46)
            clicked = true
            pause(5_000)
        }

        if ((pixel(1193, 65) == -1) && (pixel(1189, 64) == -10525848)) {  // Right flat
            log("Close")
            click(1189, 64)
            clicked = true
            pause(5_000)
        }

        if ((pixel(1236, 41) == -1) && (pixel(1236, 41) != -1)) {  // Right white
            log("Close")
            click(1236, 41)
            clicked = true
            pause(5_000)
        }

        if ((pixel(1218, 48) == -3289907) && (pixel(1231, 47) == -15000802)) {  // Right white 2
            log("Close")
            click(1236, 41)
            clicked = true
            pause(5_000)
        }

        if ((pixel(1224, 48) == -513) && (pixel(1231, 46) == -11711152)) {  // Right white 3
            log("Close")
            click(1236, 41)
            clicked = true
            pause(5_000)
        }

        if ((pixel(1223, 47) == -1) && (pixel(1231, 46) == -11711152)) {  // Right white 4
            log("Close")
            click(1236, 41)
            clicked = true
            pause(5_000)
        }

        if ((pixel(1236, 37) == -3355444) && (pixel(1252, 37) == -1)) {  // Right arrow
            log("Close")
            click(1236, 41)
            clicked = true
            pause(5_000)
        }

        if ((pixel(1236, 36) == -14540254) && (pixel(1244, 36) == -1)) {  // Right black
            log("Close")
            click(1236, 41)
            clicked = true
            pause(5_000)
        }

        if ((pixel(1234, 36) == -12697800) && (pixel(1252, 36) == -1)) {  // PUBG
            log("Close")
            click(1252, 36)
            clicked = true
            pause(5_000)
            click(1244, 34)
            pause(5_000)
        }

        if ((pixel(819, 204) == -4641000) && (pixel(645, 493) == -3405312)) {
            log("Video unavailable")
            clicker.recentButton()
            clicked = true
            pause(3_000)
        }

        if ((pixel(1230, 39) == -969700) && (pixel(1043, 663) == -7945172)) {
            log("3AM")
            click(1043, 663)
            clicked = true
            pause(5_000)
        }

        if ((pixel(1235, 21) == -308217) && (pixel(1222, 29) != -3662058) ) {
            log("Home")
            click(1235, 21)
            clicked = true
            pause(5_000)
        }

        if ((pixel(925, 652) == -3947581) && (pixel(932, 653) == -11645362)) {
            log("Recent")
            click(932, 653)
            clicked = true
            pause(5_000)
            click(38, 575)
        }

        if (pixel(638, 310) == -13967617) {
            log("Get")
            click(1160, 672)
            clicked = true
            job?.cancel()
            job = null
            pause(3_000)
        }

        if (!clicked &&
            !((pixel(925, 652) == -3947581) && (pixel(932, 653) == -11645362))
            && job == null
        ) {
            log("Dragging")
            drag(600, 360, 200, 360, 1_000)
            pause()
        }

        pause()
    }

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
                screen = getScreen()
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
            if (pixel(563, 288) == -16281669) {
                log("logged in")
                startTelegram(loginMsg, 0, false)
                pause(5_000)
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

        if (pixel(731, 346) == -4413106 //coin
            && pixel(224, 663) == -14024759 //buy button
        ) {
            log("buy")
            click(224, 663)
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
    ) {
        clicker.drag(startX, startY, endX, endY, duration)
        pause(
            duration
                    + Animator.ANIMATION_DURATION
                    + Clicker.CLICK_DURATION.toLong()
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