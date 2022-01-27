package com.posse.android.clicker.scripts

import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.Game
import com.posse.android.clicker.core.Script
import com.posse.android.clicker.scripts.base.BaseScript
import kotlinx.coroutines.delay

class FifaMobile(
    clicker: Clicker,
    script: Script,
    msg: String,
    loginMsg: String
) : BaseScript(clicker, script, msg, loginMsg) {

    private var exitCycle = false

    private var playerNumber: Int = 1

    override suspend fun run() {
        while (true) {
            super.run()
            when (script) {
                Game.MarketSell -> selling()
//                Game.MarketBuy -> buying()
                Game.EqualGame -> attack()
                Game.VSAttack -> attack()
//                Game.Test -> testClicking()
            }
        }
    }

    private fun buying() {
        TODO("Not yet implemented")
    }

//    private suspend fun testClicking() {

//    }
//        pause(2000)
//        click(400, 400)

    private suspend fun attack() {
        delay(1_000)

        if (pixel(839, 21) == -657931) {
            log("main screen")
            click(800, 340)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(645, 212) == -7498330) {
            log("enemy search is not accessible")
            click(430, 310)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(521, 302) == -1137) {
            log("lvl up")
            click(450, 430)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(483, 263) == -5458228) {
            log("lvl up reward")
            click(450, 430)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(364, 273) == -628) {
            log("vs quest")
            click(530, 410)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(471, 202) == -4537385) {
            log("network error: Orange")
            click(450, 320)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(532, 215) == -13025718) {
            log("match error")
            click(480, 310)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(246, 176) == -4471592) {
            log("performance error")
            click(450, 350)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(661, 278) == -4470564) {
            log("Division rivals")
            if (pixel(519, 105) != -7109574) {
                log("Waiting attack")
                val x = when (script) {
                    Game.EqualGame -> 490
                    Game.VSAttack -> 290
                    else -> throw RuntimeException("wrong script: $script")
                }
                click(x, 280)
                delay(2_000)
                makeScreenshot()
            }
        }

        if (pixel(519, 105) == -7109574
            && pixel(95, 434) == -1508523
        ) {
            log("Play")
            startTelegram(msg, 600_000, true)
            click(95, 434)
            val time = when (script) {
                Game.EqualGame -> 300_000L
                Game.VSAttack -> 100_000L
                else -> throw RuntimeException("wrong script: $script")
            }
            delay(time)
            makeScreenshot()
        }

        errorsCheck()
    }

    private suspend fun selling() {

        if (pixel(839, 21) == -657931) {
            log("main screen")
            click(484, 456)
            delay(3_000)
            makeScreenshot()
        }

        if (pixel(41, 77) == -320426) {
            log("market recommended")
            click(669, 59)
            delay(3_000)
            makeScreenshot()
        }

        if (pixel(736, 62) == -4984267) {
            log("sold player")
            click(250, 452)
            startTelegram(SOLD, 0, false)
            delay(3_000)
            makeScreenshot()
        }

        if (pixel(277, 377) == -15633418) {
            log("confirm")
            click(277, 377)
            delay(3_000)
            makeScreenshot()
        }

        if (pixel(668, 78) == -320426) {
            log("my orders")
            val xCoordinate = 65 + 191 * (playerNumber - 1)
            if (pixel(xCoordinate, 384) == -12823410) {
                log("player $playerNumber")
                val currentPrice = clicker.getPrice(xCoordinate, 350, xCoordinate + 120, 385)
                click(659, 65)
                delay(200)
                click(xCoordinate, 384)
                waitForColor(582, 298, -68737)
                waitForLoadedPrice()
                val minimumPrice = getLowestPrice(currentPrice)
                startTelegram(msg, delay, true)
                log("currentPrice $currentPrice")
                log("minimumPrice $minimumPrice")
                if (currentPrice != minimumPrice
                    && minimumPrice != -1
                    && currentPrice != -1
                    && minimumPrice > 999
                ) {
                    click(627, 430)
                    log("sell")
                    waitForColor(710, 165, -657931)
                    click(710, 165)
                    log("sell")
                } else {
                    click(757, 37)
                    log("close")
                }
                waitForColor(668, 78, -320426)
                playerNumber++
                if (playerNumber > PLAYERS) playerNumber = 1
            } else {
                if (playerNumber == 1) {
                    startTelegram("Все проданы", 0, false)
                    delay(3_000)
                    stop()
                }
                playerNumber = 1
            }
        }

        errorsCheck()
    }

    private suspend fun getLowestPrice(currentPrice: Int): Int {
        val minimumPrice = clicker.getPrice(660, 231, 790, 266)
        return if (checkIsPriceValid(minimumPrice, currentPrice)) minimumPrice
        else {
            val minimumPrice2 = clicker.getPrice(650, 295, 785, 330)
            if (checkIsPriceValid(minimumPrice2, currentPrice)) minimumPrice2
            else -1
        }
    }

    private fun checkIsPriceValid(price: Int, currentPrice: Int): Boolean {
        return price > currentPrice * 0.8F && price < currentPrice * 1.2F
    }

    private suspend fun errorsCheck() {

        makeScreenshot()

        if (pixel(279, 187) == -15299845    //blue
            && pixel(380, 430) == -1574059  //yellow
        ) {
            log("daily login")
            click(380, 430)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(640, 405) == -4984267) {
            log("tap to open")
            click(640, 405)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(734, 445) == -15633418
            && pixel(582, 298) != -68737
        ) {
            log("next")
            click(734, 445)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(418, 192) == -4471592) {
            log("login conflict")
            startTelegram(loginMsg, 0, false)
            delay(3_000)
            stop()
        }

        if (pixel(480, 204) == -5129779) {
            log("content update")
            click(450, 320)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(565, 180) == -7894897) {
            log("connection error")
            click(415, 300)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(247, 221) == -4471592) {
            log("connection error2")
            click(415, 300)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(239, 202) == -4471592) {
            log("connection error3")
            click(450, 320)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(551, 183) == -5526352) {
            log("maintenance")
            click(450, 290)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }
    }

    private suspend fun waitForColor(x: Int, y: Int, color: Int) {
        exitCycle = false
        while (pixel(x, y) != color && !exitCycle) {
            delay(500)
            errorsCheck()
        }
    }

    private suspend fun waitForLoadedPrice() {
        exitCycle = false
        while (clicker.getPixelCount(626, 295, 764, 320, -1) == 30 && !exitCycle) {
            delay(500)
            errorsCheck()
        }
    }

    companion object {
        private const val PLAYERS = 5
        private const val SOLD = "Игрок продан"
    }
}