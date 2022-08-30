package com.posse.android.clicker.scripts

import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.FifaGameScript
import com.posse.android.clicker.scripts.base.BaseScript
import kotlinx.coroutines.delay

class FifaMobile(
    clicker: Clicker,
    private val script: FifaGameScript,
    msg: String,
    loginMsg: String
) : BaseScript(clicker, msg, loginMsg) {

    private var exitCycle = false

    private var playerNumber: Int = 1

    override suspend fun run() {
        while (true) {
            super.run()
            when (script) {
                FifaGameScript.Market -> market()
                FifaGameScript.EqualGame -> attack()
                FifaGameScript.VSAttack -> attack()
            }
        }
    }

    private suspend fun attack() {
        delay(1_000)

        if (
            pixel(645, 212) == -7498330
            || pixel(675, 211) == -4471592
        ) {
            log("enemy search is not accessible")
            click(430, 310)
            delay(2_000)
            makeScreenshot()
            returnToHomeScreen()
        }

        if (pixel(521, 302) == -1137) {
            log("lvl up")
            click(450, 430)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(483, 263) == -5458228
            || pixel(483, 263) == -5589814
        ) {
            log("division up reward")
            click(450, 430)
            delay(2_000)
            makeScreenshot()
        }

        if (pixel(530, 428) == -15633418
            && pixel(634, 428) != -15633418
            && pixel(324, 428) != -15633418
        ) {
            log("division down")
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
            returnToHomeScreen()
        }

        if (pixel(532, 215) == -13025718
            || pixel(533, 215) == -6116675
        ) {
            log("match error")
            click(480, 310)
            delay(2_000)
            makeScreenshot()
            returnToHomeScreen()
        }

        if (pixel(246, 176) == -4471592) {
            log("performance error")
            click(450, 350)
            delay(2_000)
            makeScreenshot()
            returnToHomeScreen()
        }

        if (pixel(754, 279) == -4470564) {
            log("Division rivals")
            if (pixel(519, 105) != -7109574) {
                log("Waiting attack")
                val x = when (script) {
                    FifaGameScript.EqualGame -> 390
                    FifaGameScript.VSAttack -> 190
                    else -> throw RuntimeException("wrong script: $script")
                }
                click(x, 280)
                delay(2_000)
                makeScreenshot()
            }
        }

        if (pixel(519, 105) == -2103
            && pixel(96, 434) == -1836204
        ) {
            log("Play")
            startTelegram(msg, delay, true)
            click(95, 434)
            val time = when (script) {
                FifaGameScript.EqualGame -> 60_000L
                FifaGameScript.VSAttack -> 100_000L
                else -> throw RuntimeException("wrong script: $script")
            }
            delay(time)
            makeScreenshot()
        }

        errorsCheck()
    }

    private suspend fun returnToHomeScreen() {
        if (pixel(944, 27) == -16777216) {
            log("main screen")
            click(944, 27)
            delay(3_000)
            makeScreenshot()
        }
    }

    private suspend fun market() {

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
            if (pixel(xCoordinate, 384) == -12823410
                || pixel(xCoordinate, 384) == -14401919
            ) {
                startTelegram(msg, delay, true)
                log("player $playerNumber")
                val currentPrice = clicker.getPrice(xCoordinate, 350, xCoordinate + 120, 385)
                click(659, 65)
                delay(200)
                click(xCoordinate, 384)
                waitForLoadedPrice()
                delay(500)
                log("currentPrice $currentPrice")
                if (checkBuy()) {
                    val maximumPrice = getHighestPrice(currentPrice)
                    log("maximumPrice $maximumPrice")
                    if (currentPrice != maximumPrice
                        && maximumPrice != -1
                        && currentPrice != -1
                    ) {
                        click(627, 430)
                        log("sell")
                        waitForColor(710, 165, -657931)
                        delay(1000)
                    }
                    click(757, 37)
                    log("close")
                } else {
                    val minimumPrice = getLowestPrice(currentPrice)
                    log("minimumPrice $minimumPrice")
                    if (currentPrice != minimumPrice
                        && minimumPrice != -1
                        && currentPrice != -1
                        && minimumPrice > 999
                    ) {
                        click(627, 430)
                        log("sell")
                        waitForColor(710, 165, -657931)
                    } else {
                        click(757, 37)
                        log("close")
                    }
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

    private suspend fun waitForLoadedPrice() {
        while (clicker.getPixelCount(206, 342, 418, 437, -4984267) == 0 && !exitCycle) {
            delay(500)
            errorsCheck()
        }
    }

    private fun checkBuy() = clicker.getPixelCount(615, 91, 764, 164, -2813390) > 0

    private suspend fun getLowestPrice(currentPrice: Int): Int {
        val minimumPrice = clicker.getPrice(660, 231, 790, 266)
        return if (checkIsPriceValid(minimumPrice, currentPrice)) minimumPrice
        else {
            val minimumPrice2 = clicker.getPrice(650, 295, 785, 330)
            if (checkIsPriceValid(minimumPrice2, currentPrice)) minimumPrice2
            else -1
        }
    }

    private suspend fun getHighestPrice(currentPrice: Int): Int {
        val maximumPrice = clicker.getPrice(660, 266, 790, 301)
        return if (checkIsPriceValid(maximumPrice, currentPrice)) maximumPrice
        else {
            val maximumPrice2 = clicker.getPrice(650, 295, 785, 330)
            if (checkIsPriceValid(maximumPrice2, currentPrice)) maximumPrice2
            else -1
        }
    }

    private fun checkIsPriceValid(price: Int, currentPrice: Int): Boolean {
        return price > currentPrice * 0.8F && price < currentPrice * 1.2F
    }

    private suspend fun errorsCheck() {

        makeScreenshot()

        if (pixel(839, 21) == -657931) {
            log("main screen")
            drag(500, 500, 100, 500, 3000)
            if (script == FifaGameScript.Market) click(484, 456)
            else click(800, 340)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

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

        if (pixel(734, 445) == -15501841
            && pixel(433, 452) != -14401919
        ) {
            log("next")
            click(734, 445)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(734, 445) == -15237386) {
            log("next2")
            click(734, 445)
            delay(3_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(751, 449) == -15303436) {
            log("show all")
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

        if (pixel(475, 191) == -5985089) {
            log("unknown error")
            click(475, 335)
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

        if (pixel(710, 165) == -657931) {
            log("bet taken")
            click(710, 165)
            delay(1_000)
            exitCycle = true
            makeScreenshot()
        }


        if (pixel(234, 302) == -12823411 && pixel(722, 302) == -12823411) {
            log("big button in center")
            click(481, 309)
            delay(1_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(196, 425) == -1311659 && pixel(760, 425) == -1311659
            && pixel(186, 425) == -1311659 && pixel(770, 425) == -1311659
        ) {
            log("big yellow button in center")
            click(481, 440)
            delay(1_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(198, 430) == -1705131 && pixel(759, 430) == -1705131
            && pixel(188, 430) != -1705131 && pixel(769, 430) != -1705131
        ) {
            log("big yellow button in center2")
            click(481, 440)
            delay(1_000)
            exitCycle = true
            makeScreenshot()
        }

        if (pixel(118, 243) == -11082345) {
            log("emulator main screen")
            click(122, 144)
            delay(30_000)
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

    companion object {
        private const val PLAYERS = 5
        private const val SOLD = "Игрок продан"
    }
}