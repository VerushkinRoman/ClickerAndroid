package com.posse.android.clicker.scripts

import com.posse.android.clicker.core.Clicker
import com.posse.android.clicker.core.Game
import com.posse.android.clicker.core.Script
import com.posse.android.clicker.scripts.base.BaseScript
import kotlinx.coroutines.launch
import org.threeten.bp.LocalTime

class LooneyTunes(
    clicker: Clicker,
    script: Script,
    msg: String,
    loginMsg: String
) : BaseScript(clicker, script, msg, loginMsg) {

    override val startQuietTime: LocalTime = LocalTime.of(23, 59)
    override val endQuietTime: LocalTime = LocalTime.of(8, 0)

    override suspend fun run() {
            while (true) {
                super.run()
                when (script) {
                    Game.Ads -> looneyClicking()
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
            makeScreenshot()
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
            makeScreenshot()
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

        if ((pixel(1235, 21) == -308217) && (pixel(1222, 29) != -3662058)) {
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
}